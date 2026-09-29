# Short names for the project's scripts; the same target names are meant for every Android, iOS and Flutter project.
# `make` lists the targets. FLAVOR=<flavor> picks a flavor, e.g. `make release-apk FLAVOR=dev`.

TOOLS := packages/script-tools

.DEFAULT_GOAL := help
.PHONY: help setup tools-update tools-check release-apk release-aab release guest-e2e native-io-test

help: ## List the targets
	@grep -E '^[a-z0-9-]+:.*## ' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*## "}; {printf "  %-16s %s\n", $$1, $$2}'

setup: ## Fetch the script-tools submodule after cloning
	git submodule update --init --recursive

tools-update: ## Pull the latest script-tools; commit the new submodule pointer afterwards
	git submodule update --init --remote $(TOOLS)

tools-check:
	@test -x $(TOOLS)/android/build_release_apk.sh || { echo "$(TOOLS) is empty; run make setup first." >&2; exit 1; }

release-apk: tools-check ## Build the signed release APK into Release/
	$(TOOLS)/android/build_release_apk.sh

release-aab: tools-check ## Build the signed release AAB into Release/ for Google Play
	$(TOOLS)/android/build_release_aab.sh

release: release-aab release-apk ## Build both the AAB and the APK

guest-e2e: ## Install the guest fixture and run the engine end-to-end tests on the connected device
	scripts/run_guest_e2e.sh

native-io-test: ## Run the native IO unit test on the host
	scripts/test_native_io.sh
