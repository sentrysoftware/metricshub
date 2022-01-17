package main

import (
	"github.com/spf13/cobra"

	"go.opentelemetry.io/collector/service"
	"go.opentelemetry.io/collector/service/featuregate"
)

// NewCommand constructs a new cobra.Command using the given Collector.
func NewCommand(set service.CollectorSettings) (*cobra.Command, error) {
	rootCmd := &cobra.Command{
		Use:          set.BuildInfo.Command,
		Version:      set.BuildInfo.Version,
		SilenceUsage: true,
		RunE: func(cmd *cobra.Command, args []string) error {
			featuregate.Apply(featuregate.GetFlags())
			if set.ConfigProvider == nil {
				set.ConfigProvider = service.NewDefaultConfigProvider(getConfigFlag(), getSetFlag())
			}
			col, err := service.New(set)
			if err != nil {
				return err
			}
			return col.Run(cmd.Context())
		},
	}

	// Build a new flagSet including the default configuration
	flagSet, err := flags()
	if err != nil {
		return nil, err
	}

	rootCmd.Flags().AddGoFlagSet(flagSet)

	return rootCmd, nil
}
