package web_server;

import utils.data.IDataProvider;

import java.util.HashMap;
import java.util.Map;

public class VersionControl {
    private final String repoDirectory;
    private final IDataProvider dataProvider;
    private Map<String, String> repositories;
    private Map<String, String> repoLastVersion;
    public static int SOCKET_ERROR;
    public static int TRANSPORT_ERROR;
    public static int WRITE_ERROR;
    public static int SUCCESS;
    public static int NO_SUCH_VERSION_ERROR;
    public static int UNKNOWN_ERROR;
    public static int CONNECTION_ERROR;
    public static int NO_SUCH_REPO_ERROR;
    public static int NO_REPO_SELECTED_ERROR;


    static {
        SOCKET_ERROR = 401;
        TRANSPORT_ERROR = 402;
        WRITE_ERROR = 403;
        NO_SUCH_VERSION_ERROR = 405;
        NO_SUCH_REPO_ERROR = 404;
        NO_REPO_SELECTED_ERROR = 406;
        SUCCESS = 200;
        UNKNOWN_ERROR = 444;
        CONNECTION_ERROR = 522;
    }

    public VersionControl(IDataProvider dataProvider, String repoDirectory){
        this.repoDirectory = repoDirectory;
        this.dataProvider = dataProvider;
        dataProvider.setCurrentRoot(repoDirectory);
        repositories = new HashMap<>();
        repoLastVersion = new HashMap<>();
    }

    public String getRepoDirectory() {
        return repoDirectory;
    }

    public void createRepo(String name){
        repositories.put(name, dataProvider.resolve(repoDirectory, name));
        repoLastVersion.put(name, "");
        dataProvider.createDirectory(name);
    }

    public void updateLastVersion(String repoName, String version){
        repoLastVersion.put(repoName, version);
    }

    public String getLastVersion(String repoName){
        return repoLastVersion.get(repoName);
    }

    public String getPathToRepo(String repoName){
        return repositories.get(repoName);
    }

    public String resolve(String root, String name){
        return dataProvider.resolve(root, name);
    }

}
