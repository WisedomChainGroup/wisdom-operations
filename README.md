# wisdom-operations 

*wisdom-operations for monitoring Nodes*

## Use
*Download the latest wisdom-operation.zip in the release And decompress, run monitor.jar。Refer to operation instruction in wisdom-operation.zip for details*

```
java -jar monitor
```
## Basic function

* Authentication settings
* Basic data display
* Blockchain fork repair

*实时检测数据同步情况，如果发现有分叉的情况出现则删除对应的分叉，数据继续同步。10s/次监听绑定的节点，当发现区块哈希不满足与 2/3 邻居节点一致时，停止节点镜像删除对应区块并重启节点重新同步。*
* Operation node


