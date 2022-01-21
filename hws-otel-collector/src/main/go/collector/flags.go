package main

import (
	"flag"
	"strings"

	"go.opentelemetry.io/collector/config/configtelemetry"
	"go.opentelemetry.io/collector/service/featuregate"
)

var (
	// default
	defaultConfig = ""

	// Command-line flag that control the configuration file.
	configFlag = &defaultConfig
	setFlag    = new(valueCollection)
)

type valueCollection struct {
	values []string
}

func (v *valueCollection) Set(val string) error {
	v.values = append(v.values, val)
	return nil
}

func (v *valueCollection) String() string {
	return "[" + strings.Join(v.values, ",") + "]"
}

func flags() (*flag.FlagSet, error) {
	flagSet := new(flag.FlagSet)
	configtelemetry.Flags(flagSet)
	featuregate.Flags(flagSet)

	defaultConfig, err := getDefaultConfigFile()
	if err != nil {
		return nil, err
	}

	configFlag = flagSet.String("config", defaultConfig, "Path to the config file")

	flagSet.Var(setFlag, "set",
		"Set arbitrary component config property. The component has to be defined in the config file and the flag"+
			" has a higher precedence. Array config properties are overridden and maps are joined, note that only a single"+
			" (first) array property can be set e.g. -set=processors.attributes.actions.key=some_key. Example --set=processors.batch.timeout=2s")

	return flagSet, nil
}

func getConfigFlag() string {
	return *configFlag
}

func getSetFlag() []string {
	return setFlag.values
}
