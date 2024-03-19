keywords: develop, connector, criteria
description: This page defines the detection’s criteria that are defined in a connector.

# Device Type (Detection)

The goal of this part is to see how to define Device Type criteria.

```yaml
connector:
  # ...
  detection: # <object>
    # ...
    criteria: # <object-array>
    - type: deviceType
      keep: # <enum-array> | possible values: [ vms, osf1, hp, rs6000, linux, oob, nt, network, storage, solaris, sunos ]
      exclude: # <enum-array> | possible values: [ vms, osf1, hp, rs6000, linux, oob, nt, network, storage, solaris, sunos ]
```

### Input Properties

| Input Property | Description |
| -------------- | ----------- |
| `exclude` | List of operating systems, separated by commas, that should not match the monitored system |
| `keep` | List of operating systems, separated by commas. The monitored system's OS must match one of the listed item |

### Supported OS List

| Operating System | OS Type |
| -------------- | ------------------ |
| HP OpenVMS | `VMS` |
| HP Tru64 UNIX | `OSF1`, `tru64` |
| HP-UX | `HP`, `hpux` |
| IBM AIX | `RS600` |
| Linux (RedHat, SuSe, ESX) | `Linux` |
| Management Card/Chip, Blade Chassis, ESXi | `OOB` (Out-of Band) |
| Network | `Network` |
| Storage | `Storage` |
| Sun Solaris | `Solaris` |
| Microsoft Windows | `NT`, `win` |

### Example

```yaml
connector:
  detection:
    criteria:
    - type: deviceTYpe
      keep: [ vms, osf1, hp, rs6000, linux, oob, nt, network, storage, solaris, sunos ]
```
