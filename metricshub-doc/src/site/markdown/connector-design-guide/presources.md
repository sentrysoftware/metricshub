keywords: guide, connector, pre, presources
description:This page define sources that need to be executed before each monitoring job (discovery, multiCollect, …). These pre sources can be copied to other sources within a monitoring job.

# Pre Sources

## Format

```yaml
connector:
  # ...

pre:
 <sourceName>: # <object>
```

Each source format is defined in the [Sources Section](sources.md) Section page