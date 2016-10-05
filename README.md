Packet editor
Requires TRex scapy server

### Build
    gradle build

### Run
    gradle run

### Run Test with scapy server
    gradle cleanTest test intTest
    open ./build/reports/tests/index.html

### Run UI tests
    gradle uiTest

### Run UI tests(headless)
    gradle -Pheadless uiTest

### External Scapy server
By default, UI connects to localhost:4507 scapy server
```
#set SCAPY_SERVER env variable to use external server. default value is
SCAPY_SERVER='localhost:4507'
```

### Running scapy_server from trex-core
`./scripts/run_scapy_server -v --scapy-port 4507`

##### Run scapy_server with python
`PYTHON=python3 ./scripts/run_scapy_server -v --scapy-port 4507`

### Enable Debug logging for packed editor UI
```
cat <<ENDL > logging.properties
handlers= java.util.logging.ConsoleHandler
.level= DEBUG
ENDL
```

### Build standalone jar file
    # builds standalone ./build/libs/TRexPacketCraftingTool.jar
    gradle jar

