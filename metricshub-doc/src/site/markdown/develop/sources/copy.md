keywords: copy, clone, duplicate
description: To duplicate the content of a source in MetricsHub, use the "copy" source type.

# Copy (Source)

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
