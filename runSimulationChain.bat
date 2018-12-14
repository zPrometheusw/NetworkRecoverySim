for /L %%x in (1, 1, 30) do (
REM java  -d64   -Xms4G -Xmx4G  -XX:+UseG1GC  -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=20 -XX:ConcGCThreads=5 -XX:InitiatingHeapOccupancyPercent=70  -classpath dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataReplicationNodeFailingMain load smallworld+v+100+beta+0.5+degree+2.graph 20 0 carriersrep smallworld+v+100+beta+0.5+degree+2.loc 5000 replalgon NODELAY 0.001 6 chain 4

java  -d64   -Xms4G -Xmx4G  -XX:+UseG1GC  -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=20 -XX:ConcGCThreads=5 -XX:InitiatingHeapOccupancyPercent=70  -classpath dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataReplicationNodeFailingMainV2 load smallworld+v+100+beta+0.5+degree+2.graph 10 0 carriersrep smallworld+v+100+beta+0.5+degree+2.loc 5000 replalgon NODELAY 0.001 6 chainv2 3

REM java  -d64   -Xms4G -Xmx4G  -XX:+UseG1GC  -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=20 -XX:ConcGCThreads=5 -XX:InitiatingHeapOccupancyPercent=70  -classpath dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataReplicationNodeFailingMain load smallworld+v+100+beta+0.5+degree+2.graph 30 0 carriersrep smallworld+v+100+beta+0.5+degree+2.loc 5000 replalgon NODELAY 0.001 6 chain 2


)