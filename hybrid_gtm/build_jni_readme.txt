在做NDK開發過程中有時候在eclipse裡會遇到其無法處理inclusion導致symbol顯示錯誤，
最終的解決辦法就是初始化eclipse對該project的native support:


1. 在eclipse中關閉指定Project
2. 用其他編輯工具打開該project的.project文件，刪除以下內容：
......
<buildCommand>
<name>org.eclipse.cdt.managedbuilder.core.genmakebuilder</name>
<triggers>clean,full,incremental,</triggers>
<arguments>
</arguments>
</buildCommand>
......
<buildCommand>
<name>org.eclipse.cdt.managedbuilder.core.ScannerConfigBuilder</name>
<triggers>full,incremental,</triggers>
<arguments>
</arguments>
</buildCommand>
......
<nature>org.eclipse.cdt.core.cnature</nature>
<nature>org.eclipse.cdt.core.ccnature</nature>
<nature>org.eclipse.cdt.managedbuilder.core.managedBuildNature</nature>
<nature>org.eclipse.cdt.managedbuilder.core.ScannerConfigNature</nature>

3. 刪除.cproject文件
4. 在eclipse裡打開原來的project， refresh，然後右鍵->properties->Android Tools -> Add Native Support