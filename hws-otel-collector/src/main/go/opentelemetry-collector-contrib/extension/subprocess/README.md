# Subprocess Extension

The `subprocess` extension facilitates running a subprocess of the
collector. You are responsible for providing a configuration to the subprocess via the
command line arguments       


## Example Config

```yaml
extensions:
  health_check:
  sub_process:
    executable_path: /usr/local/bin/cmd
    args: [ "--port", "5989" ]

receivers:
# {...}
service:

  extensions: [sub_process]
```
