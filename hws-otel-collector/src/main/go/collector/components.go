package main

import (
	"go.opentelemetry.io/collector/component"
	loggingexporter "go.opentelemetry.io/collector/exporter/loggingexporter"
	datadogexporter "github.com/open-telemetry/opentelemetry-collector-contrib/exporter/datadogexporter"
	prometheusremotewriteexporter "github.com/open-telemetry/opentelemetry-collector-contrib/exporter/prometheusremotewriteexporter"
	prometheusexporter "github.com/open-telemetry/opentelemetry-collector-contrib/exporter/prometheusexporter"
	zpagesextension "go.opentelemetry.io/collector/extension/zpagesextension"
	pprofextension "github.com/open-telemetry/opentelemetry-collector-contrib/extension/pprofextension"
	healthcheckextension "github.com/open-telemetry/opentelemetry-collector-contrib/extension/healthcheckextension"
	hwsagentextension "github.com/open-telemetry/opentelemetry-collector-contrib/extension/hwsagentextension"
	memorylimiterprocessor "go.opentelemetry.io/collector/processor/memorylimiterprocessor"
	batchprocessor "go.opentelemetry.io/collector/processor/batchprocessor"
	metricstransformprocessor "github.com/open-telemetry/opentelemetry-collector-contrib/processor/metricstransformprocessor"
	filterprocessor "github.com/open-telemetry/opentelemetry-collector-contrib/processor/filterprocessor"
	resourcedetectionprocessor "github.com/open-telemetry/opentelemetry-collector-contrib/processor/resourcedetectionprocessor"
	otlpreceiver "go.opentelemetry.io/collector/receiver/otlpreceiver"
	prometheusexecreceiver "github.com/open-telemetry/opentelemetry-collector-contrib/receiver/prometheusexecreceiver"
	prometheusreceiver "github.com/open-telemetry/opentelemetry-collector-contrib/receiver/prometheusreceiver"
)

func components() (component.Factories, error) {
	var err error
	factories := component.Factories{}

	factories.Extensions, err = component.MakeExtensionFactoryMap(
		zpagesextension.NewFactory(),
		pprofextension.NewFactory(),
		healthcheckextension.NewFactory(),
		hwsagentextension.NewFactory(),
	)
	if err != nil {
		return component.Factories{}, err
	}

	factories.Receivers, err = component.MakeReceiverFactoryMap(
		otlpreceiver.NewFactory(),
		prometheusexecreceiver.NewFactory(),
		prometheusreceiver.NewFactory(),
	)
	if err != nil {
		return component.Factories{}, err
	}

	factories.Exporters, err = component.MakeExporterFactoryMap(
		loggingexporter.NewFactory(),
		datadogexporter.NewFactory(),
		prometheusremotewriteexporter.NewFactory(),
		prometheusexporter.NewFactory(),
	)
	if err != nil {
		return component.Factories{}, err
	}

	factories.Processors, err = component.MakeProcessorFactoryMap(
		memorylimiterprocessor.NewFactory(),
		batchprocessor.NewFactory(),
		metricstransformprocessor.NewFactory(),
		filterprocessor.NewFactory(),
		resourcedetectionprocessor.NewFactory(),
	)
	if err != nil {
		return component.Factories{}, err
	}

	return factories, nil
}
