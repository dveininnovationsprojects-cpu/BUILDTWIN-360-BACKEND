-- ======================================================================================
-- BUILDTWIN-360 MASTER DEMO & BENCHMARK CONSTRUCTION SEED SCRIPT
-- Project: Ashok Grandeur - Padur, OMR, Chennai (PADUR-AG-01)
-- Specification: BuildTwin 360 SRS Section 7 & Dev2 Construction Lifecycle
-- ======================================================================================

-- 1. SRS Section 7 Standard Roles
INSERT INTO roles (name) VALUES 
('ROLE_DIRECTOR'),
('ROLE_PROJECT_MANAGER'),
('ROLE_SITE_ENGINEER'),
('ROLE_SITE_SUPERVISOR'),
('ROLE_PROCUREMENT_STORE'),
('ROLE_QUANTITY_COST_COORDINATOR'),
('ROLE_QUALITY_ENGINEER'),
('ROLE_DATA_ANALYST'),
('ROLE_ADMIN'),
('ROLE_AUDITOR')
ON CONFLICT (name) DO NOTHING;

-- 2. Master Construction Project
INSERT INTO projects (code, name, description, client_name, project_type, location, status, planned_start_date, planned_end_date, estimated_budget, currency, total_built_up_area_sq_ft)
VALUES (
    'PADUR-AG-01',
    'Ashok Grandeur - Padur, Chennai',
    'Flagship 18-storey twin-tower residential community with 220 luxury units and club facilities in Padur, OMR, Chennai.',
    'Ashok Builders & Developers',
    'RESIDENTIAL',
    'Old Mahabalipuram Road (OMR), Padur, Chennai - 603103',
    'ACTIVE',
    '2026-09-01',
    '2028-06-30',
    45000000.00,
    'INR',
    350000.0
)
ON CONFLICT (code) DO NOTHING;

-- 3. Standard Construction Materials
INSERT INTO materials (material_code, name, category, unit, standard_rate, reorder_level, current_stock, description)
VALUES 
('MAT-CEM-01', 'UltraTech OPC 53 Grade Cement', 'CEMENT', 'BAGS', 380.00, 500.00, 2500.00, 'High-early strength 53-grade cement for structural concrete'),
('MAT-STL-01', 'JSW Fe500D TMT Rebar 16mm', 'STEEL', 'TONNES', 62000.00, 15.00, 65.00, 'Thermo-Mechanically Treated high ductility earthquake-resistant steel'),
('MAT-SND-01', 'Coromandel Manufactured Sand (M-Sand)', 'AGGREGATES', 'CU_M', 1650.00, 50.00, 320.00, 'Double-washed zone-II manufactured sand for plastering and concrete'),
('MAT-RMC-01', 'Ready Mix Concrete M25 Design Mix', 'CONCRETE', 'CU_M', 4800.00, 30.00, 180.00, 'Batching plant pumpable concrete mix with flyash retarder')
ON CONFLICT (material_code) DO NOTHING;

-- 4. Heavy Fleet Equipment
INSERT INTO equipment (asset_code, name, type, status)
VALUES 
('EQ-TCR-01', 'Liebherr 85EC Tower Crane (40m Jib)', 'CRANE', 'IN_USE'),
('EQ-PMP-02', 'Schwing Stetter 36m Concrete Boom Pump', 'PUMP', 'AVAILABLE'),
('EQ-EXC-03', 'JCB 3DX Backhoe Excavator', 'EXCAVATOR', 'AVAILABLE')
ON CONFLICT (asset_code) DO NOTHING;

-- 5. Registered Contractors
INSERT INTO contractors (contractor_code, name, company_name, trade_specialization, contractor_type, contact_number, email, address, status)
VALUES 
('CNT-LNT-01', 'L&T Construction (Civil Infrastructure)', 'Larsen & Toubro Ltd', 'MASON', 'MAIN_CONTRACTOR', '9876543210', 'padur.civil@intecc.com', 'L&T Construction Campus, Manapakkam, Chennai', 'ACTIVE'),
('CNT-VOL-02', 'Voltas Electro-Mechanical Solutions', 'Voltas Ltd', 'ELECTRICIAN', 'SUBCONTRACTOR', '9876543211', 'padur.mep@voltas.com', 'Voltas House, Guindy, Chennai', 'ACTIVE'),
('CNT-AP-03', 'Asian Paints Professional Services', 'Asian Paints Projects', 'HELPER', 'SUBCONTRACTOR', '9876543212', 'projects@asianpaints.com', 'Asian Paints House, Santacruz, Mumbai', 'ACTIVE')
ON CONFLICT (contractor_code) DO NOTHING;

-- 6. Construction Suppliers & Vendors
INSERT INTO suppliers (supplier_code, name, contact_person, phone, email, gstin, address, status)
VALUES 
('SUP-ULT-01', 'UltraTech Cement Ltd (Chennai Hub)', 'Ramesh Sharma', '9840112233', 'sales@ultratech.com', '33AAACU1234F1Z1', 'Chennai, Tamil Nadu', 'ACTIVE'),
('SUP-JSW-02', 'JSW Steel Ltd (Sriperumbudur Depot)', 'Vikram Mehta', '9840223344', 'rebar@jswsteel.in', '33AAACJ5678G2Z2', 'Sriperumbudur, Chennai', 'ACTIVE'),
('SUP-COR-03', 'Coromandel Aggregates & M-Sand', 'Balaji V', '9840334455', 'aggregates@coromandel.in', '33AAACC9012H3Z3', 'Chengalpattu, Tamil Nadu', 'ACTIVE')
ON CONFLICT (supplier_code) DO NOTHING;

-- 7. Cost Breakdown Structure (CBS) Baseline Budgets
INSERT INTO budgets (project_id, cost_code, cost_head, baseline_amount, description)
SELECT p.id, 'CIV_EXC_01', 'SUBCONTRACTOR', 2500000.00, 'Basement mechanical excavation and soil disposal'
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT DO NOTHING;

INSERT INTO budgets (project_id, cost_code, cost_head, baseline_amount, description)
SELECT p.id, 'CIV_RCC_01', 'MATERIAL', 15000000.00, 'Raft foundation concrete M30 and TMT Fe500 rebar'
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT DO NOTHING;

INSERT INTO budgets (project_id, cost_code, cost_head, baseline_amount, description)
SELECT p.id, 'LAB_WAGES_01', 'LABOUR', 6000000.00, 'Site skilled & unskilled labour wage allocation'
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT DO NOTHING;

INSERT INTO budgets (project_id, cost_code, cost_head, baseline_amount, description)
SELECT p.id, 'EQ_FLEET_01', 'EQUIPMENT', 3500000.00, 'Tower crane, boom pump, and DG generator diesel & hire charges'
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT DO NOTHING;

-- 8. 5-Step Project Issues Demonstration Tickets
INSERT INTO project_issues (ticket_number, project_id, title, description, issue_type, severity, status, reported_by, reported_role, initial_photo_url, target_resolution_date)
SELECT 'ISS-PADUR-NCR-01', p.id, 'Concrete 7-day cube test compressive strength fail (18.2 MPa < 20 MPa limit)',
       'Tower A raft footing batch #4 cube failed 7-day compressive test in third-party NABL lab.',
       'NCR', 'CRITICAL', 'OPEN', 'Anand (QC Lead)', 'ROLE_QUALITY_ENGINEER', 'https://storage.buildtwin360.com/evidence/padur_cube_test_failure.jpg', CURRENT_DATE + INTERVAL '2 day'
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT (ticket_number) DO NOTHING;

INSERT INTO project_issues (ticket_number, project_id, title, description, issue_type, severity, status, reported_by, reported_role, assigned_to, assigned_to_role, initial_photo_url, target_resolution_date)
SELECT 'ISS-PADUR-BLK-02', p.id, 'Local 33kV high tension transformer feeder shutdown halting Tower Crane',
       'TANGEDCO substation scheduled an unscheduled power shutdown. Crane operations stalled.',
       'BLOCKER', 'CRITICAL', 'ASSIGNED', 'Suresh Kumar (Site Eng)', 'ROLE_SITE_ENGINEER', 'Karthik Raman (PM)', 'ROLE_PROJECT_MANAGER', 'https://storage.buildtwin360.com/evidence/transformer_trip.jpg', CURRENT_DATE - INTERVAL '1 day'
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT (ticket_number) DO NOTHING;

-- 9. EVM Historical Analytics Snapshot
INSERT INTO project_evm_records (project_id, evaluation_date, planned_value, earned_value, actual_cost, budget_at_completion, cost_variance, schedule_variance, cpi, spi, estimate_at_completion, cost_status, schedule_status)
SELECT p.id, CURRENT_DATE, 18000000.00, 16500000.00, 15500000.00, 45000000.00, 1000000.00, -1500000.00, 1.065, 0.917, 42253521.00, 'UNDER_BUDGET', 'SLIGHT_DELAY'
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT (project_id, evaluation_date) DO NOTHING;

-- 10. Week 8: Quality Inspections & NCR Defects
INSERT INTO quality_issues (project_id, title, issue_type, ncr_number, location, category, severity, status, due_date, responsible_party, description, defect_photo_url)
SELECT p.id, 'Substandard rebar reinforcement tensile strength', 'NCR', 'NCR-PADUR-01', 'Tower A Raft Foundation', 'STRUCTURAL', 'CRITICAL', 'OPEN', CURRENT_DATE + INTERVAL '3 day', 'L&T Construction (Civil Div)', 'Tensile test yielded 460 MPa against mandatory 500 MPa specification. Immediate rebar replacement required.', 'https://storage.buildtwin360.com/evidence/rebar_tensile_fail.jpg'
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT DO NOTHING;

INSERT INTO quality_issues (project_id, title, issue_type, ncr_number, location, category, severity, status, due_date, responsible_party, description, defect_photo_url)
SELECT p.id, 'Concrete honeycombing on basement retaining shear wall', 'SNAG', NULL, 'Basement Retaining Wall Grid B4-B6', 'CONCRETE', 'HIGH', 'RECTIFICATION_SUBMITTED', CURRENT_DATE + INTERVAL '2 day', 'L&T Construction (Civil Div)', 'Honeycombed pockets after shutter stripping due to congested rebar and inadequate needle vibration.', 'https://storage.buildtwin360.com/evidence/honeycomb_b4.jpg'
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT DO NOTHING;

-- 11. Week 8: Project 3x3 Risk Register
INSERT INTO risks (project_id, risk_title, probability, impact, severity, risk_score, category, mitigation, contingency_plan, owner, status)
SELECT p.id, 'Deep excavation monsoon water ingress & pit flooding', 'HIGH', 'HIGH', 'CRITICAL', 9, 'SAFETY', 'Deploy 4x 15HP dewatering submersible pumps with dedicated diesel backup generator.', 'Pre-cast cutoff drainage trenches along site perimeter to divert surface runoff.', 'Karthik Raman (PM)', 'IDENTIFIED'
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT DO NOTHING;

INSERT INTO risks (project_id, risk_title, probability, impact, severity, risk_score, category, mitigation, contingency_plan, owner, status)
SELECT p.id, 'Structural steel & rebar market price volatility', 'MEDIUM', 'HIGH', 'CRITICAL', 6, 'COMMERCIAL', 'Lock in fixed quarterly procurement contract with primary producer (JSW Steel).', 'Procure advance buffer stock of 50 tonnes at current rate.', 'Director (Procurement)', 'MONITORED'
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT DO NOTHING;

INSERT INTO risks (project_id, risk_title, probability, impact, severity, risk_score, category, mitigation, contingency_plan, owner, status)
SELECT p.id, 'Grid substation power shutdown halting concrete pump & crane', 'LOW', 'HIGH', 'MEDIUM', 3, 'TECHNICAL', 'Synchronize on-site 250 kVA Caterpillar silent DG set with auto-mains failure panel.', 'Contract standby mobile generator rental provider within 1 hour SLA.', 'Plant Engineer Suresh', 'MITIGATED'
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT DO NOTHING;

-- 12. Week 8: Site Obstacles & Work Blockers
INSERT INTO issues (project_id, title, issue_type, priority, severity, owner, impact_area, days_delayed, due_date, status, cause_code, description)
SELECT p.id, 'Liebherr Tower Crane main hoist motor electrical brake failure', 'BLOCKER', 'CRITICAL', 'CRITICAL', 'Plant Engineer Suresh', 'WORKFLOW', 3, CURRENT_DATE + INTERVAL '1 day', 'OPEN', 'EQUIPMENT_BREAKDOWN', 'Main hoist brake coil burnt during heavy panel lifting. Shuttering & rebar lifting halted on Tower A.'
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT DO NOTHING;

INSERT INTO issues (project_id, title, issue_type, priority, severity, owner, impact_area, days_delayed, due_date, status, cause_code, description)
SELECT p.id, 'Delay in CMDA municipal heavy vehicle transit movement permit', 'BLOCKER', 'HIGH', 'HIGH', 'Liaison Officer Ramesh', 'SCHEDULE', 2, CURRENT_DATE + INTERVAL '2 day', 'IN_PROGRESS', 'MATERIAL_DELAY', 'Chennai traffic police restricted transit of 36m concrete boom pump during daytime peak hours. Evening transit clearance pending.'
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT DO NOTHING;

-- 13. Epic 6: Real-time Notifications & System Escalations
INSERT INTO notifications (project_id, recipient_username, target_role, title, message, notification_type, priority, status, channel, reference_module, reference_id, created_at)
SELECT p.id, 'pm_karthik', 'PROJECT_MANAGER', 'BUDGET OVERRUN ALERT: ACT-PADUR-STR-01 (CIVIL)', 'Tower A Structural Concrete expenditure reached 92.5% of allocated budget (INR 41,62,500 of INR 45,00,000).', 'BUDGET_OVERRUN', 'URGENT', 'UNREAD', 'IN_APP', 'COST_BUDGET', 101, CURRENT_TIMESTAMP
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT DO NOTHING;

INSERT INTO notifications (project_id, recipient_username, target_role, title, message, notification_type, priority, status, channel, reference_module, reference_id, created_at)
SELECT p.id, 'pm_karthik', 'PROJECT_MANAGER', 'SLA BREACH ALERT: Ticket ISS-PADUR-NCR-01', 'Issue ''Concrete 7-day cube test compressive strength fail'' assigned to Contractor is past SLA target resolution date.', 'OVERDUE_SLA', 'URGENT', 'UNREAD', 'IN_APP', 'QUALITY_ISSUE', 1, CURRENT_TIMESTAMP
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT DO NOTHING;

INSERT INTO notifications (project_id, recipient_username, target_role, title, message, notification_type, priority, status, channel, reference_module, reference_id, created_at)
SELECT p.id, 'PM_Selvamani', 'PROJECT_MANAGER', 'CRITICAL BLOCKER ALERT: Feeder Trip Halting Crane', 'Local 33kV high tension transformer feeder shutdown halting Tower Crane operations on Tower A.', 'CRITICAL_BLOCKER', 'URGENT', 'UNREAD', 'IN_APP', 'QUALITY_ISSUE', 2, CURRENT_TIMESTAMP
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT DO NOTHING;

INSERT INTO notifications (project_id, recipient_username, target_role, title, message, notification_type, priority, status, channel, reference_module, reference_id, created_at)
SELECT p.id, 'proc_ramesh', 'PROCUREMENT_STORE', 'Inventory Alert: UltraTech Cement 53 OPC (LOW_STOCK)', 'Material ''UltraTech Cement 53 Grade'' stock is 45 bags (below safety threshold of 200 bags). Shortage projected: 155 bags.', 'LOW_STOCK', 'HIGH', 'UNREAD', 'IN_APP', 'MATERIALS', 1, CURRENT_TIMESTAMP
FROM projects p WHERE p.code = 'PADUR-AG-01'
ON CONFLICT DO NOTHING;


