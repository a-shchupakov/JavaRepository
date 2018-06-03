package web_server;

import utils.data.IDataProvider;

import java.util.HashMap;
import java.util.Map;

public class VersionControl {
    private final String repoDirectory;
    private final IDataProvider dataProvider;
    private Map<String, String> repositories;
    private Map<String, String> repoLastVersion;

    private Map<String, Map<String, String>> repoVersionMapPaths; // Repo -> (Available version -> Path to version)
    private Map<String, Map<String, String[]>> repoVersionContent; // Repo -> (Available version -> Version content)
    private Map<String, Map<String, String>> repoPrevVersionMapNames; // Repo -> (Available version -> Previous version)
    private Map<String, Integer> repoPortMap; // Repo -> Free port to connect to
    private Map<String, String> repoLogFileMap;
    public static String LOG_FILE_NAME;
    public static int SOCKET_ERROR;
    public static int TRANSPORT_ERROR;
    public static int WRITE_ERROR;
    public static int SUCCESS;
    public static int NO_SUCH_VERSION_ERROR;
    public static int UNKNOWN_ERROR;
    public static int CONNECTION_ERROR;
    public static int NO_SUCH_REPO_ERROR;
    public static int NO_REPO_SELECTED_ERROR;
    public static int COMMAND_NOT_ALLOWED;
    public static int CANNOT_SAVE_LOG;


    static {
        LOG_FILE_NAME = "log.txt";
        SOCKET_ERROR = 401;
        TRANSPORT_ERROR = 402;
        WRITE_ERROR = 403;
        NO_SUCH_VERSION_ERROR = 405;
        NO_SUCH_REPO_ERROR = 404;
        NO_REPO_SELECTED_ERROR = 406;
        SUCCESS = 200;
        UNKNOWN_ERROR = 444;
        CONNECTION_ERROR = 522;
        COMMAND_NOT_ALLOWED = 433;
        CANNOT_SAVE_LOG = 412;
    }

    public VersionControl(IDataProvider dataProvider, String repoDirectory){
        this.repoDirectory = repoDirectory;
        this.dataProvider = dataProvider;
        dataProvider.setCurrentRoot(repoDirectory);
        dataProvider.clearDirectory(repoDirectory);
        repositories = new HashMap<>();
        repoLastVersion = new HashMap<>();
        repoVersionMapPaths = new HashMap<>();
        repoVersionContent = new HashMap<>();
        repoPrevVersionMapNames = new HashMap<>();
        repoPortMap = new HashMap<>();
        repoLogFileMap = new HashMap<>();
    }

    public String getRepoDirectory() {
        return repoDirectory;
    }

    public String getRepoLogFile(String repo){
        repoLogFileMap.computeIfAbsent(repo, repoName -> dataProvider.resolve(repositories.get(repoName), LOG_FILE_NAME));
        return repoLogFileMap.get(repo);
    }

    public int getRepoPort(String repo){
        repoPortMap.putIfAbsent(repo, VersionControlServer.getUnusedFreePort());
        return repoPortMap.get(repo);
    }

    public Map<String, String> getPrevVersionMapNames(String repo) {
        repoPrevVersionMapNames.putIfAbsent(repo, new HashMap<>());
        return repoPrevVersionMapNames.get(repo);
    }

    public Map<String, String> getVersionMapPaths(String repo) {
        repoVersionMapPaths.putIfAbsent(repo, new HashMap<>());
        return repoVersionMapPaths.get(repo);
    }

    public Map<String, String[]> getVersionContent(String repo) {
        repoVersionContent.computeIfAbsent(repo, repoName -> {
           Map<String, String[]> tempMap = new HashMap<>();
           tempMap.put("", new String[0]);
           return tempMap;
        });
        return repoVersionContent.get(repo);
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
        repoLastVersion.putIfAbsent(repoName, "");
        return repoLastVersion.get(repoName);
    }

    public String getPathToRepo(String repoName){
        return repositories.get(repoName);
    }

    public String resolve(String root, String name){
        return dataProvider.resolve(root, name);
    }

}
