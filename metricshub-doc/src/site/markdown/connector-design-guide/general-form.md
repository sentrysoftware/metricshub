keywords: guide, connector
description: The intended audience of this guide is the advanced user of MetricsHub, who feels confident enough to dig into the depth of connector development.

# General Form

## Connector Directory

The connector directory defines the location of the connector files. As best practice, each connector has its own directory.

## Children Pages

* [Connector Section](connectors.md)
    * [Detection Section](detection.md)
    * [Criteria Section](criteria.md)
* [Sudo Commands Section](sudo-commands.md)
* [Extends Section](extends.md)
* [Monitors Section](monitors.md)
    * [Sources Section](sources.md)
    * [Computes Section](computes.md)
    * [Mapping Section](mapping.md)
* [Metrics Section](metrics.md)
* [Constants Section](constants.md)
* [Pre Sources Section](presources.md)
* [Translation Tables Section](translation-tables.md)
* [Connector Object References Section](connector-object-references.md)

## YAML Schema

```yaml
connector:
  displayName: # <string>
  platforms: # <string>
  reliesOn: # <string>
  version:  # <string>
  projectVersion: # <string> | default: ${project.version}
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
      url: # <string>
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
          url: # <string>
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
          - type: leftConcat
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
          - type: rightConcat
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

## Embedded Files

The embedded files will be located in external files under the connector directory.