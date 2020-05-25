# wisdom-operations 

*wisdom-operations for monitoring Nodes*

## Illustrate
*Refer to the instructions in the latest wisdom-operation.zip in the release.*

## Use
*Download the latest wisdom-operation.zip in the release And decompress, run monitor.jar.*

```
java -jar monitor
```
## Basic function

* Authentication settings
```
It contains three identities: administrator, operator and query only, which are used to manage users logging in to the system.
```
* Basic data display
```
Display of basic block information and conventional indicators.
```
* Blockchain fork repair
```
On the premise of binding nodes and filling in SSH Remote connection information and database remote connection information. Real time detection of data synchronization, 10s / time monitoring of the bound node, it is found that the latest height block hash of the bound node is inconsistent with the same height block hash of the 2 / 3 neighbor node of the bound node, stop the node image to delete the corresponding block and restart the node to resynchronize.
```
* Operation node
```
On the premise of binding nodes and improving SSH Remote connection information, nodes can be stopped and restarted.
```


