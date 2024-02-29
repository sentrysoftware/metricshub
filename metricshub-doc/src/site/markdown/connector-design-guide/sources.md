keywords: guide, connector, sources
description: This page describes the format of the connector source

# Sources

This page describes the format of the connector source

* [Format](#format)
* [HTTP Source](#http_source)
* [IPMI Source](#ipmi_source)
* [OS Command Source](#os_command_source)
* [Copy](#copy)
* [Static](#static)
* [SNMPGet Source](#snmpget_source)
* [SNMP Table](#snmp_table)
* [Table Join](#table_join)
* [Table Union](#table_union)
* [WBEM Source](#wbem_source)
* [WMI Source](#wmi_source)

## Format

One or many sources can be defined under the `pre` section or under the monitor \<job\> (`discovery`, `multiCollect`, `monoCollect`, `allAtOnce`) section:

```yaml
connector:
  # ...
pre: # <object>
  <sourceKey>: # <source-object>

monitors:
  <monitorType>: # <object> 
    <job>: # <object>
      sources: # <object>
        <sourceKey>: # <source-object>
```

Under each source we can define a set of computes. Refer to the [Computes Section](connector-design-guide/computes.md) page for more details.

## HTTP Source

```yaml
connector:
  # ...
pre: # <object>
  <sourceKey>: # <source-object>

monitors:
  <monitorType>: # <object> 
    <job>: # <object>
      sources: # <object>
        <sourceKey>:
          type: http
          method: # <enum> | possible values [ get, post, delete, put ]
          url: # <string>
          path: # <string>
          header: # <string>
          body: # <string>
          authenticationToken: # <string>
          resultContent: # <enum> | possible values: [ httpStatus, header, body, all ]
          forceSerialization: <boolean>
          executeForEachEntryOf: # <object>
            source: # <string>
            concatMethod: # onOf [ <enum>, <object> ] | possible values for <enum> : [ list, json_array, json_array_extended ]
              concatStart: # <string>
              concatEnd: # <string>
          computes: # <compute-object-array>
```

## IPMI Source

```yaml
connector:
  # ...
pre: # <object>
  <sourceKey>: # <source-object>

monitors:
  <monitorType>: # <object> 
    <job>: # <object>
      sources: # <object>
        <sourceKey>:
          type: ipmi
          forceSerialization: <boolean>
          computes: # <compute-object-array>
```

## OS Command Source

```yaml
connector:
  # ...
pre: # <object>
  <sourceKey>: # <source-object>

monitors:
  <monitorType>: # <object> 
    <job>: # <object>
      sources: # <object>
        <sourceKey>:
          type: osCommand
          commandLine: # <string>
          timeout: # <number>
          executeLocally: # <boolean>
          exclude: # <string>
          keep: # <string>
          beginAtLineNumber: # <number>
          endAtLineNumber: # <number>
          separators: # <string>
          selectColumns: # <string> | comma separated values
          forceSerialization: <boolean>
          executeForEachEntryOf: # <object>
            source: # <string>
            concatMethod: # onOf [ <enum>, <object> ] | possible values for <enum> : [ list, json_array, json_array_extended ]
              concatStart: # <string>
              concatEnd: # <string>
          computes: # <compute-object-array>
```

## Copy

```yaml
connector:
  # ...
pre: # <object>
  <sourceKey>: # <source-object>

monitors:
  <monitorType>: # <object> 
    <job>: # <object>
      sources: # <object>
        <sourceKey>:
          type: copy
          from: # <string>
          computes: # <compute-object-array>
```

## Static

```yaml
connector:
  # ...
pre: # <object>
  <sourceKey>: # <source-object>

monitors:
  <monitorType>: # <object> 
    <job>: # <object>
      sources: # <object>
        <sourceKey>:
          type: static
          value: # <string>
          computes: # <compute-object-array>
```

## SNMPGet Source

```yaml
connector:
  # ...
pre: # <object>
  <sourceKey>: # <source-object>

monitors:
  <monitorType>: # <object> 
    <job>: # <object>
      sources: # <object>
        <sourceKey>:
          type: snmpGet
          oid: # <string>
          forceSerialization: <boolean>
          executeForEachEntryOf: # <object>
            source: # <string>
            concatMethod: # onOf [ <enum>, <object> ] | possible values for <enum> : [ list, json_array, json_array_extended ]
              concatStart: # <string>
              concatEnd: # <string>
          computes: # <compute-object-array>
```

## SNMP Table

```yaml
connector:
  # ...
pre: # <object>
  <sourceKey>: # <source-object>

monitors:
  <monitorType>: # <object> 
    <job>: # <object>
      sources: # <object>
        <sourceKey>:
          type: snmpTable
          oid: # <string>
          selectColumns: # <string> | comma separated values
          forceSerialization: <boolean>
          executeForEachEntryOf: # <object>
            source: # <string>
            concatMethod: # onOf [ <enum>, <object> ] | possible values for <enum> : [ list, json_array, json_array_extended ]
              concatStart: # <string>
              concatEnd: # <string>
          computes: # <compute-object-array>
```

## Table Join

```yaml
connector:
  # ...
pre: # <object>
  <sourceKey>: # <source-object>

monitors:
  <monitorType>: # <object> 
    <job>: # <object>
      sources: # <object>
        <sourceKey>:
          type: tableJoin
          leftTable: # <string>
          rightTable: # <string>
          leftKeyColumn: # <number>
          rightKeyColumn: # <number>
          defaultRightLine: # <string> | comma separated values
          isWebmKey: # <boolean>
          forceSerialization: <boolean>
          computes: <compute-object-array>
```

## Table Union

```yaml
connector:
  # ...
pre: # <object>
  <sourceKey>: # <source-object>

monitors:
  <monitorType>: # <object> 
    <job>: # <object>
      sources: # <object>
        <sourceKey>:
          type: tableUnion
          tables: # <string-array>
          forceSerialization: <boolean>
          computes: <compute-object-array>
```

## WBEM Source

```yaml
connector:
  # ...
pre: # <object>
  <sourceKey>: # <source-object>

monitors:
  <monitorType>: # <object> 
    <job>: # <object>
      sources: # <object>
        <sourceKey>:
          type: wbem
          query: # <string>
          namespace: # <string>
          forceSerialization: <boolean>
          executeForEachEntryOf: # <object>
            source: # <string>
            concatMethod: # onOf [ <enum>, <object> ] | possible values for <enum> : [ list, json_array, json_array_extended ]
              concatStart: # <string>
              concatEnd: # <string>
          computes: <compute-object-array>
```

## WMI Source

```yaml
connector:
  # ...
pre: # <object>
  <sourceKey>: # <source-object>

monitors:
  <monitorType>: # <object> 
    <job>: # <object>
      sources: # <object>
        <sourceKey>:
          type: wmi
          query: # <string>
          namespace: # <string>
          forceSerialization: <boolean>
          executeForEachEntryOf: # <object>
            source: # <string>
            concatMethod: # onOf [ <enum>, <object> ] | possible values for <enum> : [ list, json_array, json_array_extended ]
              concatStart: # <string>
              concatEnd: # <string>
          computes: <compute-object-array>
```