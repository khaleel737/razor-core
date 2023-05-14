package com.axes.razorcore.utils;

public class NUMAAwareConfiguration {
//    private final ObjectMapper objectMapper;
//    private final JsonNode config;
//
//    public NUMAAwareConfiguration(String configFile) throws IOException {
//        this.objectMapper = new ObjectMapper();
//        this.config = objectMapper.readTree(Files.readAllBytes(Paths.get(configFile)));
//    }
//
//    public String getMaxHeapSize() {
//        return config.get("maxHeapSize").asText();
//    }
//
//    public int[] getNumaNodeIds() {
//        JsonNode nodeIds = config.get("numaNodeIds");
//        int[] result = new int[nodeIds.size()];
//        for (int i = 0; i < nodeIds.size(); i++) {
//            result[i] = nodeIds.get(i).asInt();
//        }
//        return result;
//    }
//
//    public GarbageCollectorOptions getGarbageCollectorOptions() {
//        JsonNode gcOptions = config.get("garbageCollectorOptions");
//        boolean useG1GC = gcOptions.get("useG1GC").asBoolean();
//        int maxGCPauseMillis = gcOptions.get("maxGCPauseMillis").asInt();
//        int initiatingHeapOccupancyPercent = gcOptions.get("initiatingHeapOccupancyPercent").asInt();
//        int parallelism = gcOptions.get("parallelism").asInt();
//        boolean concurrentMarkEnabled = gcOptions.get("concurrentMarkEnabled").asBoolean();
//        return new GarbageCollectorOptions(useG1GC, maxGCPauseMillis, initiatingHeapOccupancyPercent, parallelism, concurrentMarkEnabled);
//    }
//
//    public HazelcastOptions getHazelcastOptions() {
//        JsonNode hazelcastOptions = config.get("hazelcastOptions");
//        String clusterName = hazelcastOptions.get("clusterName").asText();
//        JsonNode networkOptions = hazelcastOptions.get("networkOptions");
//        String publicAddress = networkOptions.get("publicAddress").asText();
//        JsonNode addresses = networkOptions.get("addresses");
//        String[] addressList = new String[addresses.size()];
//        for (int i = 0; i < addresses.size(); i++) {
//            addressList[i] = addresses.get(i).asText();
//        }
//        JsonNode mapConfigs = hazelcastOptions.get("mapConfigs");
//        HazelcastMapConfig[] mapConfigList = new HazelcastMapConfig[mapConfigs.size()];
//        for (int i = 0; i < mapConfigs.size(); i++) {
//            JsonNode mapConfig = mapConfigs.get(i);
//            String name = mapConfig.get("name").asText();
//            int timeToLiveSeconds = mapConfig.get("timeToLiveSeconds").asInt();
//            mapConfigList[i] = new HazelcastMapConfig(name, timeToLiveSeconds);
//        }
//        return new HazelcastOptions(clusterName, publicAddress, addressList, mapConfigList);
//    }
}


