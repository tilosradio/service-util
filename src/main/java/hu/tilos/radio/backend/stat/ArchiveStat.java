package hu.tilos.radio.backend.stat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

@Service
public class ArchiveStat {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveStat.class);

    public static float BYTE_PER_SECOND = Math.round(38.28125 * 836);

    private int step = 15;

    private Random random = new Random();

    private Map<LocalDateTime, Integer> counters = new TreeMap<>();

    private ResultPrinter resultPrinter = new ResultPrinter();

    @Value("${accesslog.location}")
    private String logLocation;

    public String run(String from) {

        LocalDate now = LocalDate.now();
        LocalDate startDay = LocalDate.parse(from);

        long between = ChronoUnit.DAYS.between(startDay, now);
        for (long i = 0; i < Math.ceil(between / 7.0f); i++) {
            String fileName = logLocation + "/tilos.hu-access.log";
            if (i > 0) {
                fileName = fileName + "." + i;
            }
            if (i > 1) {
                fileName = fileName + ".gz";
            }
            try {
                processFile(fileName);

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return resultPrinter.printResult(counters, startDay, step);
    }

    private void processFile(String pathname) throws IOException {
        LOGGER.info("loading " + pathname);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(unpack(pathname)))) {
            String line;
            //LocalDate start = LocalDate.of(2016, 01, 25).plusDays(7).plusDays(7);
            //LocalDate end = start.plusDays(7);
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.split("\\s+");
                    String fileName = parts[6];
                    int code = Integer.parseInt(parts[8]);
                    int size = Integer.parseInt(parts[9]);
                    String ip = parts[0];

                    LocalDate date = LocalDate.parse(parts[3].substring(1), DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss"));
                    //      if (date.isAfter(start) && date.isBefore(end)) {

                    if (line.contains("AppleCoreMedia/1.0.0.11G63")) {
                        continue;
                    }
                    if (fileName.startsWith("/sounds")) {
                        continue;
                    }
                    if (fileName.contains("mixek")) {
                        continue;
                    }
                    if (fileName.endsWith(".mp3") && !fileName.contains("stream") && !fileName.contains("online") && code < 400) {
                        analyze(fileName, code, size);

                    }
                    //    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }
    }

    private InputStream unpack(String pathname) throws IOException {
        if (pathname.contains("gz")) {
            return new GZIPInputStream(new FileInputStream(pathname));
        } else {
            return new FileInputStream(pathname);
        }
    }

    private void analyze(String fileName, int code, int size) {
        Segment segment = detectTime(fileName);
        segment = adjustWithOffset(segment, code, size);
        Map<LocalDateTime, Integer> blocks = getBlocks(segment);
        incrementCounters(blocks);
    }


    private void incrementCounters(Map<LocalDateTime, Integer> blocks) {
        blocks.keySet().forEach(dateIndex -> {
            counters.put(dateIndex, counters.getOrDefault(dateIndex, 0) + blocks.get(dateIndex));
        });
    }

    public Map<LocalDateTime, Integer> getBlocks(Segment segment) {
        Map<LocalDateTime, Integer> result = new HashMap<>();
        ZoneOffset offset = ZoneOffset.UTC;
        long divider = 60 * step;

        LocalDateTime current = segment.getStart();
        LocalDateTime end = segment.getEnd();
        while (current.isBefore(end)) {
            LocalDateTime next = getNextBlockStart(current);
            LocalDateTime before = next.minusMinutes(step);
            result.put(before, (int) ChronoUnit.SECONDS.between(current, next));
            current = next;
        }
        result.put(current.minusMinutes(step), (int) ChronoUnit.SECONDS.between(current.minusMinutes(step), end));

        return result;
    }

    public LocalDateTime getNextBlockStart(LocalDateTime current) {
        LocalDateTime next = current;

        next = next.plusMinutes(1);
        next = next.withSecond(0).withNano(0);
        while (next.getMinute() % step != 0) {
            next = next.plusMinutes(1);
        }
        return next;
    }

    private Segment adjustWithOffset(Segment segment, int code, int size) {
        if (defaultRound(segment.getStart()) && defaultRound(segment.getEnd()) && ChronoUnit.MINUTES.between(segment.getStart(), segment.getEnd()) > 60) {
            LocalDateTime newEnd = segment.getEnd().minusMinutes(30);
            if (newEnd.isBefore(segment.getStart())) {
                throw new RuntimeException("Invalid fixation " + segment);
            }
            segment.setEnd(newEnd);
        }


        long fullSeconds = ChronoUnit.SECONDS.between(segment.getStart(), segment.getEnd());
        long realSeconds = Math.round(size / BYTE_PER_SECOND);
        if (fullSeconds - realSeconds < 10 && fullSeconds < realSeconds) {
            realSeconds = fullSeconds;
        }
        long offset = 0;
        if (fullSeconds != realSeconds) {
            offset = random.nextInt((int) (fullSeconds - realSeconds));
        }

        Segment result = new Segment();
        result.setStart(segment.getStart().plusSeconds(offset));
        result.setEnd(segment.getStart().plusSeconds(offset).plusSeconds(realSeconds));
        return result;
    }

    private boolean defaultRound(LocalDateTime start) {
        return start.getSecond() == 0 && (start.getMinute() == 0 || start.getMinute() == 30);
    }

    private Pattern datePattern = Pattern.compile(".*/tilos-(\\d{4})(\\d{2})(\\d{2})-(\\d{2})(\\d{2})(\\d{2})-(\\d{2})(\\d{2})(\\d{2}).mp3");

    public Segment detectTime(String fileName) {
        Matcher matcher = datePattern.matcher(fileName);
        if (!matcher.matches()) {
            throw new RuntimeException("Invalid filename " + fileName);
        }
        Segment s = new Segment();
        s.setStart(LocalDateTime.of(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                Integer.parseInt(matcher.group(4)),
                Integer.parseInt(matcher.group(5)),
                Integer.parseInt(matcher.group(6))
        ));
        s.setEnd(LocalDateTime.of(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                Integer.parseInt(matcher.group(7)),
                Integer.parseInt(matcher.group(8)),
                Integer.parseInt(matcher.group(9))
        ));
        if (s.getEnd().isBefore(s.getStart())) {
            s.setEnd(s.getEnd().plusDays(1));
        }

        return s;
    }

    public void setStep(int step) {
        this.step = step;
    }
}