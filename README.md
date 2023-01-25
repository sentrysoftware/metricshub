# Matrix Reloaded

## Structure

This is a multi-module project:

* **/**: the root (parent of all submodules)
* **matrix-engine**: the brain, the heart of this project

## How to build the Project

### Requirements

* Have [Maven 3.x properly installed and configured](http://alpha.internal.sentrysoftware.net/lecloud/x/TwJn), with access to Sentry's repository.

### Build

To build the Matrix Reloaded package, from `./matrix-reloaded`:

```sh
$ mvn clean package
```
