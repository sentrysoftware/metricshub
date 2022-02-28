package main

import (
	"flag"
	"strings"

	"go.opentelemetry.io/collector/service/featuregate"
)

var (
	// Command-line flag that control the configuration file.
	configFlag = new(stringArrayValue)
	setFlag    = new(stringArrayValue)
)

type stringArrayValue struct {
	values []string
}

func (s *stringArrayValue) Set(val string) error {
	s.values = append(s.values, val)
	return nil
}

func (s *stringArrayValue) String() string {
	return "[" + strings.Join(s.values, ", ") + "]"
}

func flags() *flag.FlagSet {
	flagSet := new(flag.FlagSet)
	featuregate.Flags(flagSet)

	flagSet.Var(configFlag, "config", "Locations to the config file(s), note that only a"+
		" single location can be set per flag entry e.g. `-config=file:/path/to/first --config=file:path/to/second`.")

	flagSet.Var(setFlag, "set",
		"Set arbitrary component config property. The component has to be defined in the config file and the flag"+
			" has a higher precedence. Array config properties are overridden and maps are joined, note that only a single"+
			" (first) array property can be set e.g. -set=processors.attributes.actions.key=some_key. Example --set=processors.batch.timeout=2s")

	return flagSet
}

func getConfigFlag() []string {
	if len(configFlag.values) > 0 {
		return configFlag.values
	}

	defaultConfig, err := getDefaultConfigFile()
	if err != nil {
		panic(err)
	}
	return []string{defaultConfig}
}

func getSetFlag() []string {
	return setFlag.values
}
