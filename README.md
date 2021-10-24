#Simple Parser
This is a simple parser.
###Building
```
mvn clean install
alias parser='java -jar /abs/path/to/parser.jar'
```

##How-to-Use
###How-to-Run
```
parser --source /path/to/source/dir
```
###Output Format
```
ELEMENT_TYPE LOCATION PARENT_TYPE PARENT_LOCATION FILEPATH EXTRA_INFO
```
Location is a sequence of start-line, start-column, end-line, and end-column, like:
```
6    14   13   1
```

Extra_info is a list of pair of key-values separated by ";", such as:
```
visibility=public;#containedType[name=MainTest]#constructor[signature=()]
```