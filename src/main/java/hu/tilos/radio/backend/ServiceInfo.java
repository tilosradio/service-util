package hu.tilos.radio.backend;

public class ServiceInfo {
    private GitInfo git;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GitInfo getGit() {
        return git;
    }

    public void setGit(GitInfo git) {
        this.git = git;
    }
}
