# Project commands. Android targets (build, install, test, lint, apk, aab, sms, clean) come from
# packages/script-tools/android/android.mk; `make` lists them all. FLAVOR=<flavor> picks a release flavor.

# Relative to the project root: make cannot handle spaces in paths.
SCRIPT_TOOLS := packages/script-tools
# Variant for build/install/test/lint.
VARIANT := DevDebug

.PHONY: setup tools-update release guest-e2e native-io-test

# Before `make setup` the submodule is empty, so only setup and a hint are available.
ifeq ($(wildcard $(SCRIPT_TOOLS)/android/android.mk),)
.DEFAULT_GOAL := help
help:
	@echo "$(SCRIPT_TOOLS) is empty; run make setup first."
else
include $(SCRIPT_TOOLS)/android/android.mk
endif

setup: ## Fetch the script-tools submodule after cloning
	git submodule update --init --recursive

tools-update: ## Pull the latest script-tools; commit the new submodule pointer afterwards
	git submodule update --init --remote $(SCRIPT_TOOLS)

# Sub-make so each flavor runs aab and apk again, even when both targets are given at once.
release-dev: ## Signed dev AAB and APK into Release/
	$(MAKE) aab apk FLAVOR=dev

release-prod: ## Signed prod AAB and APK into Release/
	$(MAKE) aab apk FLAVOR=prod
	
guest-e2e: ## Install the guest fixture and run the engine end-to-end tests on the device
	scripts/run_guest_e2e.sh

native-io-test: ## Run the native IO unit test on the host
	scripts/test_native_io.sh
