keywords: ipmi, bmc, lan, intel
decription: The IPMI source connects to a Baseboard Management Controller (BMC) over LAN and retrieves the value of all sensors.

# IPMI (Source)

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
