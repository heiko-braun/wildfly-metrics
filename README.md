# WildFly Metrics

A self contained ^1 monitoring solution that keeps track of historical runtime data.

1. Can be used out-of-the-box, without any external dependencies.

## Prerequisites

### Wildfly 8.1.0  

Get and install Wildfly 8.1.0: http://download.jboss.org/wildfly/8.1.0.Final/wildfly-8.1.0.Final.zip

> It's currently been tested against WF 8.1.0 and the default server > configuration (standalone-monitor.xml) is configured for WF 8.
> But apart from that there should be no reason to not use it on WF 9.

## Build & Install

Build the top level project:

```
$ mvn clean install
```

Move to the monitor module

```
$ cd monitor
```

A a monitor-module.zip has been build, that can be installed on Wildfly:

```
unzip target/monitor-module.zip -d $WILDFLY_HOME
```

This will add an additional module that contains the monitor extension and subsystem:

```
modules/system/layers/base/org/wildfly/metrics/wildfly-monitor/
```

## Package Contents

The following contents will be installed when you unpack the monitor-module.zip:

```
modules/system/layers/base/org/wildfly/metrics/wildfly-monitor/main/module.xml (1)
modules/system/layers/base/org/wildfly/metrics/wildfly-monitor/main/*.jar (2)
standalone/configuration/standalone-monitor.xml (3)
domain/configuration/monitor-domain.xml (4)
domain/configuration/monitor-host.xml (5)`
```

1. The module descriptor
2. Required libraries to run the metrics subsystem on Wildfly
3. An example configuration for standalone servers
4. An example configuration for managed domains
5. An example host configuration

## Server Configuration Profiles

The monitor-module.zip server profiles for both standalone and domain mode that can be used to start a pre-configured Wildfly instance:

### Standalone Mode

```
./bin/standalone.sh -c standalone-monitor.xml -b 127.0.0.1
```

### Domain Mode

```
./bin/domain.sh --domain-config=monitor-domain.xml --host-config=monitor-host.xml -b 127.0.0.1
```

## Get In touch

The best way to reach out and discuss the monitor subsystem is the Wildfly mailing list and/or the Chat Room:

* Mailing List: https://lists.jboss.org/mailman/listinfo/wildfly-dev
* IRC: irc://freenode.org/#wildfly

## License

* http://www.apache.org/licenses/LICENSE-2.0.html

## Resources
* https://docs.jboss.org/author/display/WFLY8/Documentation

