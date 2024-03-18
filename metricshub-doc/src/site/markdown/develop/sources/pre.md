keywords: develop, connector, pre, presources
description:This page define sources that need to be executed before each monitoring job (discovery, multiCollect, …).

# *Pre* Sources

This page outlines the sources that must be run prior to each monitoring job (such as discovery, collect, simple etc.). These preliminary sources can be referenced in other sources within a monitoring job.

## Format

```yaml
connector:
  # ...

pre:
 <sourceName>: # <object>
```

Each source format is defined in the [Sources Section](sources.md) Section page.
