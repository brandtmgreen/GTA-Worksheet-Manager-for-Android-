package com.example.data

data class SampleDocument(
    val id: String,
    val title: String,
    val templateId: String,
    val description: String,
    val rawOcrText: String,
    val extractedFields: Map<String, String>,
    val clientName: String,
    val totalAmount: Double
)

object SampleDocumentRepository {
    val sampleDocuments = listOf(
        SampleDocument(
            id = "sample_greentree_estimate_04",
            title = "GreenTree Arboriculture Estimate #04",
            templateId = "estimate_sheet",
            description = "Official GreenTree Estimate for PersonJ with scheduled status, itemized tree care table, and Google review QR link.",
            rawOcrText = """
                GreenTree Arboriculture
                4314 Fay Drive Columbus, GA 31907
                greentreearboriculture.com
                mrgreen@greentreearboriculture.com
                706-505-4266

                Estimate
                Status: Scheduled
                Bill To:
                PersonJ
                4314 Fay Drive, Columbus, GA 31907
                Phone: 706-505-4266
                Email: mrgreen@greentreearboriculture.com

                Estimate #: 04
                Estimate Date: Jun 13, 2025

                # | Item | Qty/Hrs | Amount/Rate | Price
                1 | Crown Pruning & Canopy Elevation | 4.0 hrs | $175.00/hr | $700.00
                2 | Hazardous Limb Deadwooding & Rigging | 2.5 hrs | $150.00/hr | $375.00
                3 | Vermeer Brush Chipping & Wood Haulaway | 1.0 lot | $175.00/lot | $175.00

                Total: $1,250.00

                Click or scan to give us a review on Google: https://g.page/r/Ccypclex1Yp1EBM/review
                Thank you!
            """.trimIndent(),
            extractedFields = mapOf(
                "estimate_number" to "04",
                "estimate_date" to "Jun 13, 2025",
                "status" to "Scheduled",
                "client_name" to "PersonJ",
                "property_address" to "4314 Fay Drive, Columbus, GA 31907",
                "client_phone" to "706-505-4266",
                "client_email" to "mrgreen@greentreearboriculture.com",
                "line_items" to "1 | Crown Pruning & Canopy Elevation | 4.0 hrs | $175.00/hr | $700.00\n2 | Hazardous Limb Deadwooding & Rigging | 2.5 hrs | $150.00/hr | $375.00\n3 | Vermeer Brush Chipping & Wood Haulaway | 1.0 lot | $175.00/lot | $175.00",
                "amount_total" to "1250.00",
                "notes" to "Work scheduled upon signed authorization. Click or scan to give us a review on Google: https://g.page/r/Ccypclex1Yp1EBM/review\nThank you!"
            ),
            clientName = "PersonJ",
            totalAmount = 1250.00
        ),
        SampleDocument(
            id = "sample_tree_work_order",
            title = "Scanned Tree Removal & Trimming Work Order",
            templateId = "arborist_work_order",
            description = "Field work order ticket with customer details, oak tree removal, and crane crew specs.",
            rawOcrText = """
                GREENTREE ARBORICULTURE - FIELD WORK ORDER #WO-8842
                Date: 2026-06-15
                Client / Property Owner: Robert & Linda Henderson
                Contact Phone: (555) 742-9918
                Email: r.henderson@greenvalleylaw.com
                Job Site Address: 1482 Whispering Pines Rd, Oakridge, CA
                
                TREE SPECIES & QUANTITY:
                - 1x Mature Valley Oak (approx 65ft, dead top 30%, hazardous over driveway)
                - 2x Monterey Pine (crown deadwood pruning & clearance)
                
                SCOPE OF WORK:
                Full removal of Valley Oak tree down to ground level. Chipping of all brush up to 12" diameter. Log salvage on site. Safety clearance of 10ft around residential power drop.
                
                EQUIPMENT & CREW:
                55ft Bucket Truck, Vermeer 1800 Chipper, 4-Man Certified Crew, Rigging Gear.
                
                HAZARDS & CAUTION:
                Overhead primary utility lines on South perimeter (Call-before-dig #811 verified). Underground sprinkler system near front lawn. Dogs secured inside.
                
                ESTIMATE / TOTAL:
                $2,850.00
                Crew Lead: Dave Martinez (ISA #WE-9021A)
            """.trimIndent(),
            extractedFields = mapOf(
                "client_name" to "Robert & Linda Henderson",
                "client_phone" to "(555) 742-9918",
                "client_email" to "r.henderson@greenvalleylaw.com",
                "property_address" to "1482 Whispering Pines Rd, Oakridge, CA",
                "work_date" to "2026-06-15",
                "tree_species_count" to "1x Valley Oak (65ft), 2x Monterey Pine",
                "scope_of_work" to "Full removal of hazardous Valley Oak to ground level. Deadwood pruning on 2 Pines. Chipping and site cleanup.",
                "equipment_needed" to "55ft Bucket Truck, Vermeer Chipper, 4-Man Crew",
                "hazard_notes" to "Overhead utility lines on South perimeter; underground sprinklers in front lawn.",
                "amount_total" to "2850.00"
            ),
            clientName = "Robert & Linda Henderson",
            totalAmount = 2850.00
        ),
        SampleDocument(
            id = "sample_invoice",
            title = "Scanned Commercial Tree Care Invoice",
            templateId = "commercial_invoice",
            description = "Itemized invoice for commercial property pruning and stump grinding.",
            rawOcrText = """
                INVOICE #GT-2026-1049
                GreenTree Arboriculture
                Invoice Date: 2026-07-02
                Payment Due: 2026-08-01 (Net 30)
                
                BILL TO:
                Oakcrest Commercial Park LLC
                Attn: Facilities Dept (David Chen)
                Email: billing@oakcrestpark.com
                
                DESCRIPTION OF SERVICES:
                1. Annual Commercial Tree Hazard Mitigation & Pruning: $3,200.00
                2. Mechanical Stump Grinding (3 Stumps): $750.00
                3. Green Waste Hauling & Organic Mulch Distribution: $400.00
                
                SUBTOTAL: $4,350.00
                TAX / PERMIT FEES: $348.00
                TOTAL DUE: $4,698.00
                
                Payment Terms: Net 30 days. Remit to payment@greentreearboriculture.com
            """.trimIndent(),
            extractedFields = mapOf(
                "invoice_number" to "GT-2026-1049",
                "client_name" to "Oakcrest Commercial Park LLC",
                "client_email" to "billing@oakcrestpark.com",
                "invoice_date" to "2026-07-02",
                "due_date" to "2026-08-01",
                "items_description" to "1. Annual Tree Hazard Mitigation & Pruning ($3,200)\n2. Mechanical Stump Grinding 3 Stumps ($750)\n3. Waste Hauling & Mulch ($400)",
                "subtotal" to "4350.00",
                "tax_or_fees" to "348.00",
                "amount_total" to "4698.00",
                "payment_notes" to "Net 30. Remit payment to billing portal or check."
            ),
            clientName = "Oakcrest Commercial Park LLC",
            totalAmount = 4698.00
        ),
        SampleDocument(
            id = "sample_hazard_assessment",
            title = "Scanned Arborist Hazard & Safety Form",
            templateId = "hazard_safety_checklist",
            description = "Structural assessment of failing tree threatening building foundation.",
            rawOcrText = """
                ISA TREE HAZARD EVALUATION REPORT
                Inspector: Emily Green, ISA Certified Arborist #PN-4412
                Date of Inspection: 2026-07-18
                Location: 5800 Elmwood Ave, West Sector
                
                TARGET & STRUCTURES:
                Two-story residential structure, detached garage, and main power drop. High occupancy target zone.
                
                DEFECTS OBSERVED:
                Severe fungal fruiting conks (Ganoderma) around basal root flare. Longitudinal crack 4ft up main trunk. 45% crown lean toward structural roofline.
                
                POWER LINE CLEARANCE:
                High-voltage service line within 6 feet of secondary lateral branch.
                
                OVERALL RISK RATING: HIGH / IMMEDIATE ATTENTION
                
                RECOMMENDED MITIGATION:
                Schedule crane-assisted sectional dismantling within 7 days. Install temporary exclusion tape around drop zone.
            """.trimIndent(),
            extractedFields = mapOf(
                "inspector_name" to "Emily Green (ISA #PN-4412)",
                "inspection_date" to "2026-07-18",
                "site_location" to "5800 Elmwood Ave, West Sector",
                "target_potential" to "Two-story residence, detached garage, power drop",
                "structural_defects" to "Ganoderma fungal conks on root flare, 4ft longitudinal trunk crack, 45% lean to roof",
                "power_line_proximity" to "Service line within 6ft of secondary lateral branch",
                "risk_rating" to "HIGH - Urgent Action Required",
                "action_recommended" to "Crane-assisted sectional dismantling within 7 days. Zone exclusion."
            ),
            clientName = "5800 Elmwood Assessment",
            totalAmount = 0.0
        ),
        SampleDocument(
            id = "sample_phc_treatment",
            title = "Scanned Plant Health Care & Micro-Injection Log",
            templateId = "phc_treatment_log",
            description = "Trunk injection and deep root fertilization record for HOA oak woodland.",
            rawOcrText = """
                GREENTREE ARBORICULTURE - PHC & TREATMENT LOG
                Applicator / Lead: Marcus Vance (QAL #14890)
                Client / Property: Glenwood Estates HOA
                Site Address: 300 Glenwood Parkway, Woodland Sector
                Application Date: 2026-08-10
                
                DIAGNOSIS & TARGET PEST:
                Preventative Goldspotted Oak Borer (GSOB) mitigation & seasonal Iron Chlorosis in mature Quercus agrifolia.
                
                TREATMENT APPLIED:
                Trunk micro-injection of Emamectin Benzoate 4% (Mauget capsules) + Deep root liquid fertilization (12-4-8 ArborGreen with chelated micronutrients).
                
                CONDITIONS:
                Clear skies, 72°F, 2mph wind, soil moisture adequate.
                
                NEXT INSPECTION:
                2027-02-15
                
                TOTAL PHC SERVICE:
                $850.00
            """.trimIndent(),
            extractedFields = mapOf(
                "applicator_name" to "Marcus Vance (QAL #14890)",
                "client_name" to "Glenwood Estates HOA",
                "property_address" to "300 Glenwood Parkway, Woodland Sector",
                "treatment_date" to "2026-08-10",
                "target_pest_issue" to "Preventative GSOB mitigation & Iron Chlorosis in Coast Live Oaks",
                "treatment_method" to "Trunk micro-injection Emamectin Benzoate 4% + Deep Root ArborGreen 12-4-8",
                "weather_conditions" to "Clear, 72°F, 2mph wind",
                "follow_up_date" to "2027-02-15",
                "amount_total" to "850.00"
            ),
            clientName = "Glenwood Estates HOA",
            totalAmount = 850.00
        ),
        SampleDocument(
            id = "sample_emergency_storm",
            title = "Scanned Emergency Storm & Crane Rigging Ticket",
            templateId = "emergency_storm_log",
            description = "Urgent response document for fallen Douglas Fir onto residential roof.",
            rawOcrText = """
                GREENTREE 24/7 EMERGENCY STORM RESPONSE
                Incident #: EM-2026-0904
                Homeowner: Margaret Sullivan
                Phone: (555) 302-8811
                Site Location: 910 Highland Ridge Rd
                Incident Date: 2026-08-25
                
                STRUCTURAL IMPACT:
                85ft Douglas Fir root-plate failure across master bedroom roof and garage ridge. High hazard tension on trunk.
                
                RIGGING & CRANE OPERATIONS:
                50-Ton Crane deployed (4.5 billable hours) with 4-man certified aerial rigging crew. Sectional piece-out completed without secondary roof deflection.
                
                MITIGATION & STABILIZATION:
                Full industrial tarping installed over breached roofline; plywood load spreading installed on deck.
                
                EMERGENCY SERVICE TOTAL:
                $5,200.00 (Insurance Claim Pre-Authorized)
            """.trimIndent(),
            extractedFields = mapOf(
                "incident_number" to "EM-2026-0904",
                "client_name" to "Margaret Sullivan",
                "client_phone" to "(555) 302-8811",
                "site_address" to "910 Highland Ridge Rd",
                "storm_event_date" to "2026-08-25",
                "structure_impact" to "85ft Douglas Fir root failure over master bedroom roof",
                "crane_rigging_details" to "50-Ton Crane (4.5 hrs) + 4-man certified rigging crew",
                "tarp_mitigation" to "Industrial waterproof tarp & plywood load spreading installed",
                "amount_total" to "5200.00"
            ),
            clientName = "Margaret Sullivan",
            totalAmount = 5200.00
        )
    )
}
