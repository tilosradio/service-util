package hu.tilos.radio.backend.stat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ResultPrinter {

    public static final String SEPARATOR = ",";

    protected String printResult(Map<LocalDateTime, Integer> counters, LocalDate startDay, int step) {
        String[][] lines = new String[60 / step * 24 + 1][8];
        LocalDate currentDay = startDay;
        LocalDate endDay = startDay.plusDays(7);

        int idx = 0;
        LocalDateTime currentHour = currentDay.atTime(0, 0);
        LocalDateTime endHour = currentHour.plusDays(1);

        while (currentHour.isBefore(endHour)) {
            lines[idx + 1][0] = String.valueOf(currentHour.format(DateTimeFormatter.ofPattern("HH:mm"))) + SEPARATOR;
            currentHour = currentHour.plusMinutes(step);
            idx++;
        }


        int column = 1;
        while (currentDay.isBefore(endDay)) {
            lines[0][column] = String.valueOf(currentDay) + SEPARATOR;
            int line = 1;
            LocalDateTime currentTime = currentDay.atTime(0, 0);
            LocalDateTime endTime = currentTime.plusDays(1);
            while (currentTime.isBefore(endTime)) {
                if (counters.get(currentTime) != null) {
                    lines[line][column] = (counters.get(currentTime) / (step * 60)) + SEPARATOR;
                } else {
                    lines[line][column] = "0" + SEPARATOR;
                }
                line++;
                currentTime = currentTime.plusMinutes(step);
            }

            currentDay = currentDay.plusDays(1);
            column++;
        }

        StringBuilder b = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            for (int j = 0; j < lines[i].length; j++) {
                if (lines[i][j] != null) {
                    b.append(lines[i][j]);
                } else {
                    b.append(SEPARATOR);
                }
            }
            b.append("\n");
        }
        return b.toString();
    }
}
