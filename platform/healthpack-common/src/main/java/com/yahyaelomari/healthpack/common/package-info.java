/**
 * Cross-cutting plumbing shared by every service: the error model, correlation IDs,
 * the transactional outbox, the event envelope, and security configuration.
 *
 * <p>Domain classes never belong here. Two services sharing an entity are coupled
 * through it, which is exactly what splitting them apart was meant to avoid.
 *
 * <p>Beans declared here are not picked up by a service's component scan, because
 * services live in sibling packages. Register them as auto-configuration in
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}.
 */
package com.yahyaelomari.healthpack.common;
