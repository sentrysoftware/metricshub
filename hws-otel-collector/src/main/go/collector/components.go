package main

import (
	datadogexporter "github.com/open-telemetry/opentelemetry-collector-contrib/exporter/datadogexporter"
	prometheusexporter "github.com/open-telemetry/opentelemetry-collector-contrib/exporter/prometheusexporter"
	prometheusremotewriteexporter "github.com/open-telemetry/opentelemetry-collector-contrib/exporter/prometheusremotewriteexporter"
	signalfxexporter "github.com/open-telemetry/opentelemetry-collector-contrib/exporter/signalfxexporter"
	basicauthextension "github.com/open-telemetry/opentelemetry-collector-contrib/extension/basicauthextension"
	healthcheckextension "github.com/open-telemetry/opentelemetry-collector-contrib/extension/healthcheckextension"
	hwsagentextension "github.com/open-telemetry/opentelemetry-collector-contrib/extension/hwsagentextension"
	pprofextension "github.com/open-telemetry/opentelemetry-collector-contrib/extension/pprofextension"
	filterprocessor "github.com/open-telemetry/opentelemetry-collector-contrib/processor/filterprocessor"
	metricstransformprocessor "github.com/open-telemetry/opentelemetry-collector-contrib/processor/metricstransformprocessor"
	resourcedetectionprocessor "github.com/open-telemetry/opentelemetry-collector-contrib/processor/resourcedetectionprocessor"
	prometheusexecreceiver "github.com/open-telemetry/opentelemetry-collector-contrib/receiver/prometheusexecreceiver"
	prometheusreceiver "github.com/open-telemetry/opentelemetry-collector-contrib/receiver/prometheusreceiver"
	"go.opentelemetry.io/collector/component"
	loggingexporter "go.opentelemetry.io/collector/exporter/loggingexporter"
	otlpexporter "go.opentelemetry.io/collector/exporter/otlpexporter"
	otlphttpexporter "go.opentelemetry.io/collector/exporter/otlphttpexporter"
	zpagesextension "go.opentelemetry.io/collector/extension/zpagesextension"
	batchprocessor "go.opentelemetry.io/collector/processor/batchprocessor"
	memorylimiterprocessor "go.opentelemetry.io/collector/processor/memorylimiterprocessor"
	otlpreceiver "go.opentelemetry.io/collector/receiver/otlpreceiver"
)

func components() (component.Factories, error) {
	var err error
	factories := component.Factories{}

	factories.Extensions, err = component.MakeExtensionFactoryMap(
		zpagesextension.NewFactory(),
		pprofextension.NewFactory(),
		healthcheckextension.NewFactory(),
		hwsagentextension.NewFactory(),
		basicauthextension.NewFactory(),
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
		otlpexporter.NewFactory(),
		otlphttpexporter.NewFactory(),
		loggingexporter.NewFactory(),
		datadogexporter.NewFactory(),
		prometheusremotewriteexporter.NewFactory(),
		prometheusexporter.NewFactory(),
		signalfxexporter.NewFactory(),
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
