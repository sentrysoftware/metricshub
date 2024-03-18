keywords: develop, connector, source, reference
description: This page shows how to reference connector objects such as sources, entries, columns and files, etc. inside the YAML document.

# Connector Object References

<div class="alert alert-warning"><span class="fa-solid fa-person-digging"></span> Documentation under construction...</div>

<!-- MACRO{toc|fromDepth=1|toDepth=1|id=toc} -->

This page shows how to reference connector objects such as sources, entries, columns and files, etc. inside the YAML document.

## Source Reference

### Format

A Source can be referenced either with its full path or its relative path. A relative path can be used only if the referenced source is in the same monitor and same job as the current source.

### Example

```yaml
      mapping:
        source: ${esc.d}{source::monitors.disk_controller.discovery.sources.source_discovery}
```

```yaml
        sourceDiscovery:
          type: tableJoin
          leftTable: ${esc.d}{source::monitors.enclosure.collect.sources.source_chassis} # full path for a source in another monitor
          rightTable: ${esc.d}{source::source_enclosure} # relative path
          leftKeyColumn: 1
          rightKeyColumn: 1
```

## Entry Reference

### Format

```yaml
$<columnNumber>
```

### Example

```yaml
        source(3):
          type: http
          method: GET
          executeForEachEntryOf:
            source: ${esc.d}{source::monitors.enclosure.discovery.sources.source(2)}
            concatMethod: list
          path: /api/rest/StorageCenter/ScChassis/$2/PowerSupplyList
```

## Column Reference

### Format

```yaml
$<columnNumber>
```

### Example

```yaml
      mapping:
        # PowerSupply
        # tableID;ID;DisplayName;objectType;enclosure/controllerID;deviceType
        source: ${esc.d}{source::monitors.power_supply.discovery.sources.source(4)}
        attributes:
          id: $2
          __display_id: $3
          hw.parent.type: $6
          hw.parent.id: $5
          name: $3
```

## File Reference

### Format

```yaml
${esc.d}{file::<relativeFilePath>}
```

### Example

```yaml
   criteria:
    - type: osCommand
      commandLine: /bin/sh ${esc.d}{file::storman-drives.sh}
      expectedResult: Hard drive
      errorMessage: No Adaptec Controller with Physical Disks attached or not enough rights to execute arcconf.
```

## Mono-Instance Reference

### Format

```yaml
${esc.d}{attribute::<attribute-key>}
```

### Example

```yaml
 collect:
      # Collect type is multi-instance
      type: monoInstance
      sources:
        source(1):
          type: osCommand
          commandLine: /bin/sh ${esc.d}{file::script.sh} ${esc.d}{attribute::id}
          keep: ^MSHW;
          separators: ;
          selectColumns: "2,3,4,5,6,7,8,9"
```

## Translation Table Reference

### Format

```yaml
${esc.d}{translation::<translationTable>}
```

### Example

```yaml
    collect:
      # Collect type = multi-instance
      type: multiInstance
      sources:
        source(1):
          # Source(1) = connUnitSensorTable SNMP Table
          # ID;Status;Value;
          type: snmpTable
          oid: 1.3.6.1.4.1.1588.2.1.1.1.1.22.1
          selectColumns: "ID,3,4"
          computes:
            # Translate the first column status into a PATROLStatus
            # ID;PatrolStatus;Value;
          - type: translate
            column: 2
            translationTable: ${esc.d}{translation::SensorStatusTranslationTable}
```

## Awk Script Reference

### Format

```yaml
${esc.d}{awk::<script>}
```

### Example

```yaml
    monitors:
      battery:
        discovery:
          mapping:
            source: ${esc.d}{source::monitors.battery.discovery.sources.source(1)}
            attributes:
              name: ${esc.d}{awk::sprintf("%s (%s)", "Cisco", $1)}
```
