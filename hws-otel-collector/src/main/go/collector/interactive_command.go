package main

import (
	"github.com/spf13/cobra"

	"go.opentelemetry.io/collector/service"
	"go.opentelemetry.io/collector/service/featuregate"
)

// NewCommand constructs a new cobra.Command using the given Collector.
func NewCommand(set service.CollectorSettings) *cobra.Command {
	rootCmd := &cobra.Command{
		Use:          set.BuildInfo.Command,
		Version:      set.BuildInfo.Version,
		SilenceUsage: true,
		RunE: func(cmd *cobra.Command, args []string) error {
			featuregate.GetRegistry().Apply(gatesList)
			col, err := newCollectorWithLogCore(set)
			if err != nil {
				return err
			}
			return col.Run(cmd.Context())
		},
	}

	rootCmd.Flags().AddGoFlagSet(flags())

	return rootCmd
}
