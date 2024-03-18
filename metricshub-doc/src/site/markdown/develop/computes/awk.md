keywords: awk, gawk, jawk
description: The "awk" compute operation processes a source through the specified AWK script in MetricsHub.

# `awk`

The `awk` compute allows to process the table through an awk script.
The script to execute can be put in a separate file for more readability and be called in the script value.

Eg: ```yaml script: ${esc.d}{file::my-script.awk}``` for a script named `my-script.awk` placed in the directory of your connector.

`keep` and `exclude` can be used to trim your result values with regular expressions.

`separators` and `selectColumns` can be used to separate your result value into multiple columns and keep only those of your choice.

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
          computes: # <compute-object-array>
          - type: awk
            script: # <string>
            exclude:  # <string>
            keep: # <string>
            separators: # <string>
            selectColumns: # <string> | comma separated values
```

## Example

In this example, we will process our source through an Awk script.
Each line that results from the execution of this script will then be filtered using the regular expression and separated into multiple columns using the ';' character as a separator and keep columns only from 2 to 12.

```yaml
          - type: awk
            script: "${esc.d}{file::embeddedFile-1}"
            keep: ^MSHW;
            separators: ;
            selectColumns: "2,3,4,5,6,7,8,9,10,11,12"
```
