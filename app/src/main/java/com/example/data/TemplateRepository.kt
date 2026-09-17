package com.example.data

object TemplateRepository {
    val templates: List<Template> = listOf(
        Template(
            id = "estimate_sheet",
            title = "Estimate",
            category = "Sales & Estimates",
            description = "Official GreenTree Arboriculture estimate sheet with bill to, itemized pricing table, scheduled status, and Google Review QR code.",
            defaultTitlePrefix = "Estimate",
            fields = listOf(
                TemplateField("estimate_number", "Estimate #", "text", "", "e.g. 04", listOf("estimate #", "estimate no", "est #", "quote #", "number")),
                TemplateField("estimate_date", "Estimate Date", "date", "", "e.g. Jun 13, 2025", listOf("date", "estimate date", "issue date")),
                TemplateField("status", "Status", "text", "Scheduled", "e.g. Scheduled / Draft / Sent / Accepted", listOf("status", "job status", "state")),
                TemplateField("client_name", "Bill To (Client Name)", "text", "", "e.g. PersonJ", listOf("bill to", "client", "customer", "name", "recipient")),
                TemplateField("property_address", "Service / Property Address", "text", "", "e.g. 4314 Fay Drive, Columbus, GA 31907", listOf("address", "property", "site", "location")),
                TemplateField("client_phone", "Client Phone", "phone", "", "e.g. 706-505-4266", listOf("phone", "tel", "cell")),
                TemplateField("client_email", "Client Email", "email", "", "e.g. mrgreen@greentreearboriculture.com", listOf("email", "e-mail")),
                TemplateField("line_items", "Itemized Table (# | Item | Qty/Hrs | Rate | Price)", "multiline", "", "1 | Crown Pruning & Canopy Elevation | 4.0 hrs | $175/hr | $700.00\n2 | Hazardous Limb Deadwooding & Rigging | 2.5 hrs | $150/hr | $375.00\n3 | Vermeer Brush Chipping & Wood Haulaway | 1.0 lot | $175/lot | $175.00", listOf("items", "item", "qty", "hrs", "rate", "price", "description", "services", "line items")),
                TemplateField("amount_total", "Total ($)", "currency", "", "e.g. 1250.00", listOf("total", "price", "amount", "grand total", "total estimate", "estimate total")),
                TemplateField("notes", "Notes / Special Instructions", "multiline", "All work performed to ANSI A300 standards. Fully licensed & insured.", "e.g. Standard terms & review link included", listOf("notes", "terms", "instructions"))
            )
        ),
        Template(
            id = "arborist_work_order",
            title = "Tree Service Work Order",
            category = "Arboriculture",
            description = "Complete job ticket for tree trimming, removal, pruning, and stump grinding.",
            defaultTitlePrefix = "Tree Work Order",
            fields = listOf(
                TemplateField("client_name", "Client / Property Owner", "text", "", "e.g. Robert Henderson", listOf("client", "customer", "name", "bill to", "property owner")),
                TemplateField("client_phone", "Contact Phone", "phone", "", "e.g. (555) 742-9918", listOf("phone", "tel", "cell", "mobile", "contact")),
                TemplateField("client_email", "Email Address", "email", "", "e.g. r.henderson@example.com", listOf("email", "e-mail", "mail")),
                TemplateField("property_address", "Job Site Address", "text", "", "e.g. 1482 Whispering Pines Rd, Oakridge, CA", listOf("address", "site", "location", "job site", "property")),
                TemplateField("work_date", "Scheduled Date", "date", "", "YYYY-MM-DD", listOf("date", "scheduled", "work date", "job date")),
                TemplateField("tree_species_count", "Tree Species & Quantity", "text", "", "e.g. 1x Mature Valley Oak (65ft), 2x Monterey Pine", listOf("species", "trees", "quantity", "tree type")),
                TemplateField("scope_of_work", "Scope of Work / Service Details", "multiline", "", "e.g. Full removal of Valley Oak down to ground level; crown deadwood reduction on 2 Pines.", listOf("scope", "description", "service", "work description", "details", "work to be performed")),
                TemplateField("equipment_needed", "Equipment & Crew Required", "text", "", "e.g. 55ft Bucket Truck, Vermeer 1800 Chipper, 4-Man Crew", listOf("equipment", "crew", "machinery", "tools")),
                TemplateField("hazard_notes", "Safety & Hazard Notes", "multiline", "", "e.g. Overhead utility lines on East side; underground sprinklers in front lawn.", listOf("hazards", "safety", "warnings", "risks", "power lines", "caution")),
                TemplateField("amount_total", "Total Estimate / Price ($)", "currency", "", "e.g. 2850.00", listOf("total", "price", "amount", "cost", "estimate", "grand total", "balance"))
            )
        ),
        Template(
            id = "arborist_estimate_proposal",
            title = "Arborist Estimate & Proposal",
            category = "Sales & Proposals",
            description = "Detailed client quote with scope breakdown, pruning specs, and approval terms.",
            defaultTitlePrefix = "Estimate Proposal",
            fields = listOf(
                TemplateField("proposal_number", "Proposal / Quote #", "text", "", "e.g. PROP-2026-441", listOf("proposal #", "quote #", "estimate #", "quote number")),
                TemplateField("client_name", "Client / Organization", "text", "", "e.g. Sarah Jenkins", listOf("client", "name", "customer", "property owner")),
                TemplateField("client_phone", "Client Phone", "phone", "", "e.g. (555) 987-6543", listOf("phone", "tel", "cell")),
                TemplateField("client_email", "Client Email", "email", "", "e.g. s.jenkins@example.com", listOf("email", "e-mail")),
                TemplateField("site_address", "Service Address", "text", "", "e.g. 842 Oak Canyon Way", listOf("address", "site", "location")),
                TemplateField("proposal_date", "Proposal Date", "date", "", "YYYY-MM-DD", listOf("date", "proposal date", "created date")),
                TemplateField("scope_options", "Proposed Options & Tiers", "multiline", "", "Option 1: Complete Oak Removal ($2,400)\nOption 2: Crown Clearance & Weight Reduction ($1,100)", listOf("scope", "options", "proposal details", "services")),
                TemplateField("debris_disposal", "Debris & Wood Disposal", "text", "All brush chipped and removed; logs hauled", "e.g. Chipped on site / logs left for firewood", listOf("debris", "disposal", "cleanup", "logs")),
                TemplateField("deposit_required", "Deposit Required ($)", "currency", "0.00", "e.g. 500.00", listOf("deposit", "down payment", "retainer")),
                TemplateField("amount_total", "Total Proposed Price ($)", "currency", "", "e.g. 2400.00", listOf("total", "price", "amount", "estimate total", "grand total"))
            )
        ),
        Template(
            id = "commercial_invoice",
            title = "Commercial Tree Care Invoice",
            category = "Billing",
            description = "Professional invoice for arboriculture services rendered, labor, parts, and net terms.",
            defaultTitlePrefix = "Commercial Invoice",
            fields = listOf(
                TemplateField("invoice_number", "Invoice #", "text", "", "e.g. GT-2026-1049", listOf("invoice #", "inv #", "invoice no", "number")),
                TemplateField("client_name", "Bill To (Client / Company)", "text", "", "e.g. Oakcrest Commercial Park LLC", listOf("bill to", "client", "customer", "company", "name")),
                TemplateField("client_email", "Billing Email", "email", "", "e.g. billing@oakcrestpark.com", listOf("email", "billing email", "e-mail")),
                TemplateField("invoice_date", "Invoice Date", "date", "", "YYYY-MM-DD", listOf("date", "invoice date", "issue date")),
                TemplateField("due_date", "Payment Due Date", "date", "", "YYYY-MM-DD or Net 30", listOf("due date", "terms", "pay by")),
                TemplateField("items_description", "Itemized Services / Goods", "multiline", "", "e.g. 1. Annual Commercial Tree Hazard Mitigation: $3,200\n2. Mechanical Stump Grinding: $750", listOf("description", "items", "services", "line items", "details")),
                TemplateField("subtotal", "Subtotal ($)", "currency", "", "e.g. 3950.00", listOf("subtotal", "sub-total", "net amount")),
                TemplateField("tax_or_fees", "Tax / Extra Fees ($)", "currency", "0.00", "e.g. 316.00", listOf("tax", "sales tax", "fees", "disposal fee")),
                TemplateField("amount_total", "Total Due ($)", "currency", "", "e.g. 4266.00", listOf("total", "amount due", "total due", "grand total", "balance due")),
                TemplateField("payment_notes", "Payment Terms & Method", "text", "Net 30 - Remit to GreenTree Arboriculture", "e.g. Direct ACH, check, or online card payment", listOf("terms", "payment method", "notes", "remit to"))
            )
        ),
        Template(
            id = "hazard_safety_checklist",
            title = "Tree Hazard & Risk Assessment (TRAQ)",
            category = "Safety & Inspection",
            description = "ISA Level 2 tree risk assessment, structural defect evaluation, and utility clearance.",
            defaultTitlePrefix = "Hazard Assessment",
            fields = listOf(
                TemplateField("inspector_name", "Certified Inspector / Arborist", "text", "", "e.g. Emily Green, ISA Certified #PN-4412", listOf("inspector", "arborist", "assessor", "evaluator")),
                TemplateField("inspection_date", "Assessment Date", "date", "", "YYYY-MM-DD", listOf("date", "inspection date")),
                TemplateField("site_location", "Site / Tree Location", "text", "", "e.g. 5800 Elmwood Ave, West Sector", listOf("location", "address", "site")),
                TemplateField("target_potential", "Target / Vulnerability", "text", "", "e.g. Two-story residence, detached garage, high pedestrian zone", listOf("target", "structures", "traffic", "risk area")),
                TemplateField("structural_defects", "Observed Defects & Health", "multiline", "", "e.g. Ganoderma conks on root flare, 4ft longitudinal crack, 45% crown lean", listOf("defects", "decay", "cavity", "cracks", "fungi", "observations")),
                TemplateField("power_line_proximity", "Utility / Power Line Clearance", "text", "Clear (>10 ft)", "e.g. High-voltage service line within 6ft of lateral limb", listOf("utility", "power line", "clearance", "electrical")),
                TemplateField("risk_rating", "Overall Risk Rating", "text", "High", "e.g. Low / Moderate / High / Extreme", listOf("risk rating", "risk level", "rating", "priority")),
                TemplateField("action_recommended", "Mitigation Action Plan", "multiline", "", "e.g. Crane-assisted sectional dismantling within 7 days. Zone exclusion.", listOf("action", "recommendation", "mitigation", "plan"))
            )
        ),
        Template(
            id = "phc_treatment_log",
            title = "Plant Health Care (PHC) & Soil Treatment Log",
            category = "Plant Health Care",
            description = "Fertilization, trunk micro-injection, fungal/insect management, and soil amendment log.",
            defaultTitlePrefix = "PHC Treatment Log",
            fields = listOf(
                TemplateField("applicator_name", "Certified Applicator / Tech", "text", "", "e.g. Marcus Vance (QAL #14890)", listOf("applicator", "technician", "lead", "operator")),
                TemplateField("client_name", "Client / Property", "text", "", "e.g. Glenwood Estates HOA", listOf("client", "property", "hoa", "name")),
                TemplateField("property_address", "Site Address", "text", "", "e.g. 300 Glenwood Parkway", listOf("address", "site", "location")),
                TemplateField("treatment_date", "Application Date", "date", "", "YYYY-MM-DD", listOf("date", "treatment date", "application date")),
                TemplateField("target_pest_issue", "Diagnosis / Target Condition", "text", "", "e.g. Emerald Ash Borer prevention / Iron chlorosis", listOf("pest", "diagnosis", "disease", "condition", "target")),
                TemplateField("treatment_method", "Application Method & Product", "text", "", "e.g. Trunk Micro-injection (Emamectin Benzoate 4%) + Deep Root Iron Chelate", listOf("product", "chemical", "treatment", "method", "dosage")),
                TemplateField("weather_conditions", "Weather / Wind Speed", "text", "Clear, 68°F, Wind 3mph", "e.g. Temp, wind speed, soil moisture", listOf("weather", "wind", "temp", "conditions")),
                TemplateField("follow_up_date", "Next Scheduled Inspection", "date", "", "YYYY-MM-DD", listOf("follow up", "next inspection", "re-treatment")),
                TemplateField("amount_total", "Treatment Fee ($)", "currency", "", "e.g. 850.00", listOf("total", "price", "fee", "cost", "amount"))
            )
        ),
        Template(
            id = "emergency_storm_log",
            title = "Emergency Storm Response & Crane Log",
            category = "Emergency Services",
            description = "Hazardous storm damage triage, tree on structure rigging, and insurance claims ticket.",
            defaultTitlePrefix = "Emergency Storm Log",
            fields = listOf(
                TemplateField("incident_number", "Incident / Claim #", "text", "", "e.g. EM-2026-0904", listOf("incident #", "claim #", "emergency #")),
                TemplateField("client_name", "Homeowner / Insured", "text", "", "e.g. Margaret Sullivan", listOf("client", "homeowner", "insured", "name")),
                TemplateField("client_phone", "Emergency Contact Phone", "phone", "", "e.g. (555) 302-8811", listOf("phone", "emergency phone", "cell")),
                TemplateField("site_address", "Incident Location", "text", "", "e.g. 910 Highland Ridge Rd", listOf("address", "location", "site")),
                TemplateField("storm_event_date", "Storm / Incident Date", "date", "", "YYYY-MM-DD", listOf("date", "storm date", "incident date")),
                TemplateField("structure_impact", "Structural Impact / Emergency Level", "text", "Tree on Roof / Urgent", "e.g. 80ft Douglas Fir uprooted across master bedroom roof", listOf("damage", "structure", "impact", "emergency level")),
                TemplateField("crane_rigging_details", "Crane & Specialized Rigging Hours", "text", "50-Ton Crane (4.5 hrs) + 4 Techs", "e.g. Crane setup, blind pick, tag line rigging", listOf("crane", "rigging", "hours", "equipment")),
                TemplateField("tarp_mitigation", "Tarping & Stabilization", "text", "Heavy-duty waterproof tarp installed", "e.g. Plywood sheeting & structural cribbing", listOf("tarp", "stabilization", "mitigation")),
                TemplateField("amount_total", "Emergency Service Total ($)", "currency", "", "e.g. 5200.00", listOf("total", "emergency total", "invoice total", "amount"))
            )
        ),
        Template(
            id = "equipment_inspection",
            title = "Daily Equipment & Chipper Safety Checklist",
            category = "Fleet & Maintenance",
            description = "Pre-trip and post-trip machinery check for chainsaws, chipper, bucket truck, and crane.",
            defaultTitlePrefix = "Equipment Checklist",
            fields = listOf(
                TemplateField("operator_name", "Operator / Crew Lead", "text", "", "e.g. Marcus Vance", listOf("operator", "driver", "crew lead", "inspector")),
                TemplateField("vehicle_equipment_id", "Equipment ID / Unit #", "text", "", "e.g. Vermeer BC1800XL Chipper / Bucket Truck #7", listOf("unit", "equipment", "truck #", "vehicle")),
                TemplateField("fluids_and_fuel", "Fluids & Fuel Level", "text", "Checked & Full", "e.g. Engine oil OK, Hydraulic oil full, Bar oil topped", listOf("fluids", "fuel", "oil", "coolant")),
                TemplateField("safety_devices", "Emergency Stops & Safety Guards", "text", "Operational", "e.g. Bottom feed stop bar tested, breakaway cables secure", listOf("safety", "guards", "e-stop", "interlocks")),
                TemplateField("ppe_check", "PPE Compliance (Chaps, Helmets, Eye)", "text", "100% Verified", "e.g. Verified for all 4 crew members", listOf("ppe", "helmets", "protection", "gear")),
                TemplateField("defects_or_repairs", "Issues / Repairs Needed", "multiline", "None detected", "e.g. Left headlight loose; tension chain on saw #2", listOf("defects", "maintenance", "repairs", "issues"))
            )
        ),
        Template(
            id = "client_consultation",
            title = "Client Consultation & Property Assessment",
            category = "Sales & CRM",
            description = "Field meeting summary, tree inventory markers, customer requirements, and follow-up tasks.",
            defaultTitlePrefix = "Consultation Note",
            fields = listOf(
                TemplateField("client_name", "Client Name", "text", "", "e.g. Dr. Sarah Jenkins", listOf("client", "name", "contact")),
                TemplateField("client_phone", "Phone Number", "phone", "", "e.g. (555) 987-6543", listOf("phone", "cell", "tel")),
                TemplateField("meeting_date", "Consultation Date", "date", "", "YYYY-MM-DD", listOf("date", "meeting date")),
                TemplateField("property_address", "Property Address", "text", "", "e.g. 742 Evergreen Terrace", listOf("address", "property", "location")),
                TemplateField("client_goals", "Client Goals & Concerns", "multiline", "", "e.g. Wants more sunlight on solar panels; worried about dead oak branch over driveway", listOf("goals", "concerns", "request", "objective", "wants")),
                TemplateField("tree_inventory", "Tree Inventory / Notes", "multiline", "", "e.g. 1x mature Pin Oak (40ft), 3x Italian Cypress along fence line", listOf("trees", "inventory", "species")),
                TemplateField("follow_up_action", "Next Steps / Follow-up", "text", "Send proposal by Friday", "e.g. Email formal bid with 3 tier options", listOf("follow up", "next steps", "action item", "todo"))
            )
        )
    )

    fun getTemplateById(id: String): Template {
        return templates.find { it.id == id } ?: templates.first()
    }
}

