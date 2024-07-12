keywords: develop, connector
description: The intended audience of this guide is the advanced user of MetricsHub, who feels confident enough to dig into the depth of connector development.

# Connector

<div class="alert alert-warning"><span class="fa-solid fa-person-digging"></span> Documentation under construction...</div>

<!-- MACRO{toc|fromDepth=1|toDepth=1|id=toc} -->

## Connector Directory

The connector directory defines the location of the connector files. As best practice, each connector has its own directory that will contain the connector and its embedded files.

All connector directories are to be placed in the "MetricsHub/connectors" directory for them to be exploited by MetricsHub.

## The Connector Object

<!-- MACRO{toc|fromDepth=1|toDepth=1|id=toc} -->

The first part of a connector is a list of information defining its identity, what is the purpose of this connector and what type of platforms and systems it can be used against. The goal of this part is for someone to understand quickly what this connector is about.

### Format

```yaml
connector:
  displayName: # <string>
  platforms: # <string>
  reliesOn: # <string>
  version:  # <string>
  projectVersion: # <string> | default: {esc.d}${project.version}
  information: # <string>
  detection: # <object>
```

### Properties

| Property              | Description              |
| --------------------- | ------------------------ |
| `displayName` | Name of the connector, which can be displayed in the console or reported in the metric’s attribute.(`connector.status{name="Dell OpenManage (WMI)"}`)<br />This preferably refers to the underlying instrumentation layer (e.g.: Dell OpenManage (WMI)).<br />If several connectors are required to cover different aspects of a platform (one connector for the CPU, memory, another for the disks, and a last one for the network cards, for example), the name will specify it with a dash separator.<br /><br />The typical display name therefore looks like:<br />`<Instrumentation Layer> [ - Subcomponent ] [ - OS ] [ (protocol) ]`. |
| `information` | Describes what the connector monitors and how.<br /><br /> This ends up in the documentation of the Hardware Connector Library as the description of the connector.<br /> Do not hesitate to provide details about the specific requirements for the connector to work properly. |
| `platforms` | Typical targeted system.<br /> Examples: "`HP ProLiant`" or "`Any system with SNMP`"<br /> This property is leveraged to build the Supported Platforms in the documentation.<br /> The platform name must be short and simple enough to group several connectors targeting the same type of systems.<br /> Several platforms can be specified in a comma-separated list.<br /> Connectors that monitor components that may be present in large number of platforms (e.g.: the <br /> connector which monitors network cards in all Windows systems) must specify: `Any system with [xxx]`. |
| `reliesOn` | Name of the instrumentation layer this connector leverages. This can also be considered as the <br /> technical prerequisites for this connector work, but it can only mention one instrumentation layer. <br /><br />This also ends up in the documentation and in the [Supported Platforms](../platform-requirements.html) and it is important that all<br /> connectors have a consistent wording for this property. |
| `projectVersion` | The current version of the connector library project. |
| `version` | The current version of the connector. |
| `detection` | Defines all the information required to perform connector’s detection. See specification in [Detection](detection.md). |

## Embedded Files

The embedded files will be located in external files under the connector directory.

## SUDO Commands

This page defines how to specify in a connector the sudo-able commands.

### Format

```yaml
connector:
  # ...
sudoCommands: <string-array>
```

## Constants

This page describes how to declare constant in a connector file.

## Format

```yaml
connector:
  # ...
connector:
  # ...
constants: # <object>
  <constantName>: # <string>
```

## Extends

To defines a set of connectors to extends, you need to declare the connectors under the `extends` sections.

## Format

```yaml
connector:
  # ...
extends: <string-array>
```

In the extends array, you need to declare connectors using their file name without the .yaml extension.
Either a relative path can be used to point to the extended connector, or an absolute path, using the `connectors` directory as root.

## YAML Schema

```yaml
connector:
  displayName: # <string>
  platforms: # <string>
  reliesOn: # <string>
  version:  # <string>
  projectVersion: # <string> | default: {esc.d}${project.version}
  information: # <string>

  detection: # <object>
    connectionTypes: # <enum-array> | possible values: [ remote, local ] | default: local
    disableAutoDetection: # <boolean> | default: false
    onLastResort: # <string>
    appliesTo: # <enum-array> | possible values: [ vms, osf1, hp, rs6000, linux, oob, nt, network, storage, solaris, sunos. ]
    supersedes: # <string-array>

    criteria: # <criterion-object-array>
    # Http criterion
    - type: http
      method: # <enum> | possible values: [ get, post, delete, put ]
      path: # <string>
      header: # <string>
      body: # <string>
      expectedResult: # <string>
      errorMessage: # <string>
      resultContent: # <enum> | possible values: [ httpStatus, header, body, all ] | default: body
      authenticationToken: # <string>
      forceSerialization: # <boolean>
    # Ipmi criterion
    - type: ipmi
    # productVersion criterion
    - type: productRequirements
      kmVersion: # <string>
      engineVersion: # <string>
    # Os criterion
    - type: deviceType
      keep: # <enum-array> | possible values: [ vms, osf1, hp, rs6000, linux, oob, nt, network, storage, solaris, sunos ]
      exclude: # <enum-array> | possible values: [ vms, osf1, hp, rs6000, linux, oob, nt, network, storage, solaris, sunos ]
    # OsCommand criterion
    - type: osCommand
      commandLine: # <string>
      errorMessage: # <string>
      expectedResult: # <string>
      executeLocally: # <boolean>
      timeout: # <number>
      forceSerialization: # <boolean>
    # Process criterion
    - type: process
      commandLine: # <string>
    # Service criterion
    - type: service
      name: # <string>
    # SnmpGet criterion
    - type: snmpGet
      oid: # <string>
      expectedResult: # <string>
      forceSerialization: # <boolean>
    # SnmpGetNext criterion
    - type: snmpGetNext
      oid: # <string>
      expectedResult: # <string>
      forceSerialization: # <boolean>
    # Ucs criterion
    - type: ucs
      query: # <string>
      errorMessage: # <string>
      expectedResult: # <string>
      forceSerialization: # <boolean>
    # Wbem criterion
    - type: wbem
      query: # <string>
      namespace: # <string> | default: root/cimv2
      errorMessage: # <string>
      expectedResult: # <string>
      forceSerialization: # <boolean>
    # Wmi criterion
    - type: wmi
      query: # <string>
      namespace: # <string> | default: root/cimv2
      errorMessage: # <string>
      expectedResult: # <string>
      forceSerialization: # <boolean>

sudoCommands: # <string-array>

extends: # <string-array>

metrics:
  <metricName>: # <object>
    unit: # <string>
    description: # <string>
    type: # oneOf [ <enum>, <object> ] | possible values for <enum> [ Gauge, Counter, UpDownCounter ]
      stateSet: # <string-array>
      output: # <enum> | possible values [ Gauge, Counter, UpDownCounter ] | Optional | Default: UpDownCounter



constants: # <object>
  <constantName>: # <string>

pre: # <object>
  <sourceKey>: # <source-object>

monitors:
  <monitorType>: # <object>
    <job>: # <object> | <job> key possible values [ discovery, collect, simple]
      type: # <string> | Only for collect <job> | possible values [ multiInstance, monoInstance ]
      keys: # <string-array> | Only for collect <job> with multiInstance type | Default: [ id ]
      # Sources
      sources: # <source-object>
        # Http Source
        <http-sourceKey>: # <source-object>
          type: http
          method: # <enum> | possible values: [ get, post, delete, put ]
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
        # Ipmi Source
        <ipmi-sourceKey>: # <source-object>
          type: ipmi
          forceSerialization: <boolean>
          computes: # <compute-object-array>
        # OsCommand Source
        <osCommand-sourceKey>: # <source-object>
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
        # Copy Source
        <copy-sourceKey>: # <source-object>
          type: copy
          from: # <string>
          computes: # <compute-object-array>
        # Static Source
        <static-sourceKey>: # <source-object>
          type: static
          value: # <string>
          computes: # <compute-object-array>
        # SnmpGet source
        <snmpGet-sourceKey>: # <source-object>
          type: snmpGet
          oid: # <string>
          forceSerialization: <boolean>
          executeForEachEntryOf: # <object>
            source: # <string>
            concatMethod: # onOf [ <enum>, <object> ] | possible values for <enum> : [ list, json_array, json_array_extended ]
              concatStart: # <string>
              concatEnd: # <string>
          computes: # <compute-object-array>
        <snmpTable-sourceKey>: # <source-object>
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
        # TableJoin Source
        <tableJoin-sourceKey>: # <source-object>
          type: tableJoin
          leftTable: # <string>
          rightTable: # <string>
          leftKeyColumn: # <number>
          rightKeyColumn: # <number>
          defaultRightLine: # <string> | comma separated values
          isWbemKey: # <boolean>
          forceSerialization: <boolean>
          computes: <compute-object-array>
        # TableUnion Source
        <tableUnion-sourceKey>: # <source-object>
          type: tableUnion
          tables: # <string-array>
          forceSerialization: <boolean>
          computes: <compute-object-array>
        # Ucs Source
        <ucs-sourceKey>: # <source-object>
          type: ucs
          queries: # <string-array>
          exclude: # <string>
          keep: # <string>
          selectColumns: # <string>  | comma separated values
          forceSerialization: <boolean>
          executeForEachEntryOf: # <object>
            source: # <string>
            concatMethod: # onOf [ <enum>, <object> ] | possible values for <enum> : [ list, json_array, json_array_extended ]
              concatStart: # <string>
              concatEnd: # <string>
          computes: <compute-object-array>
        # Wbem Source
        <wbem-sourceKey>: # <source-object>
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
        # Wmi Source
        <wmi-sourceKey>: # <source-object>
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
        # SQL Source
        <sql-sourceKey>: # <source-object>
        <sourceKey>:
          type: sql
          tables: <sqltable-object-array>
          - source: <string>
            alias: <string>
            columns: <sqlcolumn-object-array>
            - name: <string>
              number: <integer>
              type: <string>
          query: <string>
          computes: <compute-object-array>

        # Computes
        <sourceKey>: # <source-object>
          computes: <compute-object-array>
          - type: add
            column: # <number>
            value:  # <string>
          - type: and
            column: # <number>
            value:  # <string>
          - type: arrayTranslate
            column: # <number>
            translationTable:  # <string>
            arraySeparator: # <string>
            resultSeparator: # <string>
          - type: awk
            script: # <string>
            exclude:  # <string>
            keep: # <string>
            separators: # <string>
            selectColumns: # <string> | comma separated values
          - type: convert
            column: # <number>
            conversion: # <enum> | possible values: [ hex2Dec, array2SimpleStatus ]
          - type: divide
            column: # <number>
            value: # <String>
          - type: duplicateColumn
            column: # <number>
          - type: excludeMatchingLines
            column: # <number>
            regExp: # <string>
            valueList: # <string> | comma separated values
          - type: extract
            column: # <number>
            subColumn: # <number>
            subSeparators: # <string>
          - type: extractPropertyFromWbemPath
            property: # <string>
            column: # <number>
          - type: json2Csv
            entryKey: # <string>
            properties: # <string> | comma separated values
            separator: # <string> | default: ";"
          - type: keepColumns
            columnNumbers: # <string> | comma separated values
          - type: keepMatchingLines
            column: # <number>
            regExp: # <string>
            valueList: # <string> | comma separated values
          - type: prepend
            column: # <number>
            value: # <string>
          - type: multiply
            column: # <number>
            value: # <string>
          - type: perBitTranslation
            column: # <number>
            bitList: # <string> | comma separated values
            translationTable: # <string>
          - type: replace
            column: # <number>
            existingValue: # <string>
            newValue: # <string>
          - type: append
            column: # <number>
            value: # <string>
          - type: subtract
            column: # <number>
            value: # <string>
          - type: substring
            column: # <number>
            start: # <string>
            length: # <string>
          - type: translate
            column: # <number>
            translationTable:  # <string>
          - type: xml2Csv
            recordTag: # <string>
            properties:  # <string> | comma separated values

      mapping:
        source: # <string>
        attributes:
          <key>: # <string>
        conditionalCollection:
          <key>: # <string>
        metrics:
          <key>: # <string>
        legacyTextParameters:
          <key>: # <string>

translations: # <object>
  <table-key>: # <object>
    <key>: # <string>
```

