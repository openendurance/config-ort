/*
 * SPDX-FileCopyrightText: 2026 Open Endurance Initiative
 * SPDX-License-Identifier: Apache-2.0
 */

/*******************************************************
 * Open Endurance Initiative — ORT evaluator rules      *
 *                                                       *
 * Reads categories from license-classifications.yml    *
 * and turns them into Approve / Flag / Deny outcomes.   *
 * Consult legal counsel before relying on this for      *
 * anything beyond engineering triage.                   *
 *******************************************************/

val permissiveLicenses = licenseClassifications.licensesByCategory["permissive"].orEmpty()
val permissiveEmergingLicenses = licenseClassifications.licensesByCategory["permissive-emerging"].orEmpty()
val linkingExceptionLicenses = licenseClassifications.licensesByCategory["copyleft-with-linking-exception"].orEmpty()
val publicDomainLicenses = licenseClassifications.licensesByCategory["public-domain"].orEmpty()
val attributionContentLicenses = licenseClassifications.licensesByCategory["attribution-content"].orEmpty()
val weakCopyleftLicenses = licenseClassifications.licensesByCategory["weak-copyleft"].orEmpty()
val strongCopyleftLicenses = licenseClassifications.licensesByCategory["strong-copyleft"].orEmpty()

// Licenses that are approved outright — no issue raised.
val approvedLicenses = listOf(
    permissiveLicenses,
    permissiveEmergingLicenses,
    linkingExceptionLicenses,
    publicDomainLicenses
).flatten()

// The complete set of licenses covered by a classification, approved or not.
// Anything NOT in this set falls through to UNHANDLED_LICENSE below.
val handledLicenses = listOf(
    approvedLicenses,
    attributionContentLicenses,
    weakCopyleftLicenses,
    strongCopyleftLicenses
).flatten().let {
    it.getDuplicates().let { duplicates ->
        require(duplicates.isEmpty()) {
            "The classifications for the following licenses overlap: $duplicates"
        }
    }
    it.toSet()
}

/**
 * Standard "how to fix" guidance, linked to the actual OEI workflow rather than generic boilerplate.
 */
fun PackageRule.howToFixDefault() = """
    If this license should be approved org-wide, open a PR against `config-ort` adding it to the
    appropriate category in `license-classifications.yml`.

    If ORT has mis-detected this package's license (wrong SPDX id, missing metadata), the fix is a
    correction in `curations.yml`, not a classification change — these are different problems with
    different fixes.

    Either way, changes to `config-ort` require review per its CODEOWNERS — this isn't a
    self-service override.
""".trimIndent()

fun PackageRule.LicenseRule.isHandled() =
    object : RuleMatcher {
        override val description = "isHandled($license)"
        override fun matches() = license in handledLicenses
    }

fun PackageRule.LicenseRule.isApproved() =
    object : RuleMatcher {
        override val description = "isApproved($license)"
        override fun matches() = license in approvedLicenses
    }

fun PackageRule.LicenseRule.isAttributionContent() =
    object : RuleMatcher {
        override val description = "isAttributionContent($license)"
        override fun matches() = license in attributionContentLicenses
    }

fun PackageRule.LicenseRule.isWeakCopyleft() =
    object : RuleMatcher {
        override val description = "isWeakCopyleft($license)"
        override fun matches() = license in weakCopyleftLicenses
    }

fun PackageRule.LicenseRule.isStrongCopyleft() =
    object : RuleMatcher {
        override val description = "isStrongCopyleft($license)"
        override fun matches() = license in strongCopyleftLicenses
    }

val ruleSet = ruleSet(ortResult, licenseInfoResolver) {

    // Catch-all: anything not in license-classifications.yml at all. This is the rule that
    // replaces FOSSA's "unrecognized license" flagging — except here, fixing it is a config-ort
    // PR you control, not a $200/mo plan upgrade.
    packageRule("UNHANDLED_LICENSE") {
        require {
            -isExcluded()
        }

        licenseRule("UNHANDLED_LICENSE", LicenseView.CONCLUDED_OR_DECLARED_AND_DETECTED) {
            require {
                -isExcluded()
                -isHandled()
            }

            error(
                "The license $license is currently not covered by any category in " +
                    "license-classifications.yml. The license was ${licenseSource.name.lowercase()} in " +
                    "package ${pkg.metadata.id.toCoordinates()}.",
                howToFixDefault()
            )
        }
    }

    // Declared license strings that don't parse as valid SPDX — a data-quality signal worth
    // surfacing even though it's not itself a policy violation.
    packageRule("UNMAPPED_DECLARED_LICENSE") {
        require {
            -isExcluded()
        }

        resolvedLicenseInfo.licenseInfo.declaredLicenseInfo.processed.unmapped.forEach { unmappedLicense ->
            warning(
                "The declared license '$unmappedLicense' could not be mapped to a valid SPDX " +
                    "expression, in package ${pkg.metadata.id.toCoordinates()}.",
                howToFixDefault()
            )
        }
    }

    // attribution-content: approved for build-time use, flagged so a human confirms it never
    // ends up in a distributed artifact (CC-BY-4.0 is the live example right now).
    packageRule("ATTRIBUTION_CONTENT_LICENSE") {
        require {
            -isExcluded()
        }

        licenseRule("ATTRIBUTION_CONTENT_LICENSE", LicenseView.CONCLUDED_OR_DECLARED_AND_DETECTED) {
            require {
                -isExcluded()
                +isAttributionContent()
            }

            hint(
                "Package ${pkg.metadata.id.toCoordinates()} carries the content/attribution license " +
                    "$license. Confirm it's build-time-only (never bundled into a distributed artifact) " +
                    "before treating this as resolved.",
                howToFixDefault()
            )
        }
    }

    // weak-copyleft: flagged for review, not auto-denied — matters more here than it would for
    // an internal tool, since `core` is itself a redistributed library.
    packageRule("WEAK_COPYLEFT_LICENSE") {
        require {
            -isExcluded()
        }

        licenseRule("WEAK_COPYLEFT_LICENSE", LicenseView.CONCLUDED_OR_DECLARED_AND_DETECTED) {
            require {
                -isExcluded()
                +isWeakCopyleft()
            }

            issue(
                Severity.WARNING,
                "Package ${pkg.metadata.id.toCoordinates()} is licensed under $license " +
                    "(weak copyleft) via its ${licenseSource.name.lowercase()} license.",
                howToFixDefault()
            )
        }
    }

    // strong-copyleft: denied by default for Apache-2.0 library/platform code.
    packageRule("STRONG_COPYLEFT_LICENSE") {
        require {
            -isExcluded()
        }

        licenseRule("STRONG_COPYLEFT_LICENSE", LicenseView.CONCLUDED_OR_DECLARED_OR_DETECTED) {
            require {
                -isExcluded()
                +isStrongCopyleft()
            }

            error(
                "Package ${pkg.metadata.id.toCoordinates()} is licensed under $license " +
                    "(strong copyleft), via its ${licenseSource.name.lowercase()} license. Denied by " +
                    "default policy.",
                howToFixDefault()
            )
        }
    }

    // Same strong-copyleft check, scoped to transitive dependencies rather than the project's
    // own declared/detected license.
    dependencyRule("STRONG_COPYLEFT_IN_DEPENDENCY") {
        licenseRule("STRONG_COPYLEFT_IN_DEPENDENCY", LicenseView.CONCLUDED_OR_DECLARED_OR_DETECTED) {
            require {
                +isStrongCopyleft()
            }

            error(
                "Project ${project.id.toCoordinates()} has dependency ${dependency.id.toCoordinates()} " +
                    "licensed under the strong-copyleft license $license.",
                howToFixDefault()
            )
        }
    }
}

ruleViolations += ruleSet.violations
