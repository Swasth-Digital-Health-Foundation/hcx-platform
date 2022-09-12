package io.hcxprotocol.validator;

import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.hcxprotocol.createresource.HCXInsurancePlan;
import org.fhir.ucum.Value;
import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r4.model.*;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;

public class HCXFHIRValidator {

    private static HCXFHIRValidator instance = null;

    private FhirValidator validator = null;

    private HCXFHIRValidator() throws Exception {
        FhirContext fhirContext = FhirContext.forR4();
        fhirContext.setDefaultTypeForProfile("https://ig.hcxprotocol.io/v0.7/StructureDefinition-HCXInsurancePlan.html", HCXInsurancePlan.class);
        // Create a chain that will hold the validation modules
        System.out.println("we have started");
        ValidationSupportChain supportChain = new ValidationSupportChain();

        // DefaultProfileValidationSupport supplies base FHIR definitions. This is generally required
        // even if we are using custom profiles, since those profiles will derive from the base
        // definitions.
        DefaultProfileValidationSupport defaultSupport = new DefaultProfileValidationSupport(fhirContext);
        supportChain.addValidationSupport(defaultSupport);

        // This module supplies several code systems that are commonly used in validation
        supportChain.addValidationSupport(new CommonCodeSystemsTerminologyService(fhirContext));

        // This module implements terminology services for in-memory code validation
        supportChain.addValidationSupport(new InMemoryTerminologyServerValidationSupport(fhirContext));

        IParser parser = fhirContext.newJsonParser();
        String hcxIGBasePath = "https://ig.hcxprotocol.io/v0.7/";
        String nrcesIGBasePath = "https://nrces.in/ndhm/fhir/r4/";
        // test: load HL7 base definition
        //StructureDefinition sdCoverageEligibilityRequest = (StructureDefinition) parser.parseResource(new URL("http://hl7.org/fhir/R4/coverageeligibilityrequest.profile.json").openStream());
        StructureDefinition sdCoverageEligibilityRequest = (StructureDefinition) parser.parseResource(new URL(hcxIGBasePath + "StructureDefinition-CoverageEligibilityRequest.json").openStream());
        StructureDefinition sdCoverageEligibilityResponse = (StructureDefinition) parser.parseResource(new URL(hcxIGBasePath + "StructureDefinition-CoverageEligibilityResponse.json").openStream());
        StructureDefinition sdClaim = (StructureDefinition) parser.parseResource(new URL(hcxIGBasePath + "StructureDefinition-Claim.json").openStream());
        StructureDefinition sdNRCESPatient = (StructureDefinition) parser.parseResource(new URL(nrcesIGBasePath + "StructureDefinition-Patient.json").openStream());
        StructureDefinition sdInsurancePlan = (StructureDefinition) parser.parseResource(new URL(hcxIGBasePath + "StructureDefinition-HCXInsurancePlan.json").openStream());
        StructureDefinition sdHCXProofOfIdentificationExtension = (StructureDefinition) parser.parseResource(new URL(hcxIGBasePath + "StructureDefinition-HCXProofOfIdentificationExtension.json").openStream());
        StructureDefinition sdHCXProofOfPresenceExtension = (StructureDefinition) parser.parseResource(new URL(hcxIGBasePath + "StructureDefinition-HCXProofOfPresenceExtension.json").openStream());
        StructureDefinition sdHCXDiagnosticDocumentsExtension = (StructureDefinition) parser.parseResource(new URL(hcxIGBasePath + "StructureDefinition-HCXDiagnosticDocumentsExtension.json").openStream());
        StructureDefinition sdHCXInformationalMessagesExtension = (StructureDefinition) parser.parseResource(new URL(hcxIGBasePath + "StructureDefinition-HCXInformationalMessagesExtension.json").openStream());
        StructureDefinition sdHCXQuestionnairesExtension = (StructureDefinition) parser.parseResource(new URL(hcxIGBasePath + "StructureDefinition-HCXQuestionnairesExtension.json").openStream());

        //adding the value sets
        ValueSet vsProofOfIdentity = (ValueSet) parser.parseResource(new URL(hcxIGBasePath + "ValueSet-proof-of-identity-codes.json").openStream());
        ValueSet vsProofOfPresence = (ValueSet) parser.parseResource(new URL(hcxIGBasePath + "ValueSet-proof-of-presence-codes.json").openStream());
        ValueSet vsClinicalDiagnostics = (ValueSet) parser.parseResource(new URL(hcxIGBasePath + "ValueSet-clinical-diagnostics-document-codes.json").openStream());
        ValueSet vsInformationalMessages = (ValueSet) parser.parseResource(new URL(hcxIGBasePath + "ValueSet-informational-messages-codes.json").openStream());


        // Create a PrePopulatedValidationSupport which can be used to load custom definitions.
        PrePopulatedValidationSupport prePopulatedSupport = new PrePopulatedValidationSupport(fhirContext);
        prePopulatedSupport.addStructureDefinition(sdCoverageEligibilityRequest);
        prePopulatedSupport.addStructureDefinition(sdCoverageEligibilityResponse);
        prePopulatedSupport.addStructureDefinition(sdClaim);
        prePopulatedSupport.addStructureDefinition(sdNRCESPatient);
        prePopulatedSupport.addStructureDefinition(sdInsurancePlan);
        prePopulatedSupport.addStructureDefinition(sdHCXProofOfIdentificationExtension);
        prePopulatedSupport.addStructureDefinition(sdHCXProofOfPresenceExtension);
        prePopulatedSupport.addStructureDefinition(sdHCXDiagnosticDocumentsExtension);
        prePopulatedSupport.addStructureDefinition(sdHCXInformationalMessagesExtension);
        prePopulatedSupport.addStructureDefinition(sdHCXQuestionnairesExtension);
        prePopulatedSupport.addValueSet(vsClinicalDiagnostics);
        prePopulatedSupport.addValueSet(vsInformationalMessages);
        prePopulatedSupport.addValueSet(vsProofOfPresence);
        prePopulatedSupport.addValueSet(vsProofOfIdentity);

        // Add the custom definitions to the chain
        supportChain.addValidationSupport(prePopulatedSupport);
        CachingValidationSupport cache = new CachingValidationSupport(supportChain);

        // Create a validator using the FhirInstanceValidator module.
        FhirInstanceValidator validatorModule = new FhirInstanceValidator(cache);
        this.validator = fhirContext.newValidator().registerValidatorModule(validatorModule);
    }

    private static HCXFHIRValidator getInstance() throws Exception {
        if (null == instance) 
            instance = new HCXFHIRValidator();

        return instance;
    }

    public static FhirValidator getValidator() throws Exception {
        return getInstance().validator;
    }

    public static void main(String[] args) {

        try {
            FhirValidator validator = HCXFHIRValidator.getValidator();

            //creating a new HCXInsurancePlan object and validating it against the HCX SD

            HCXInsurancePlan ip =  new HCXInsurancePlan();;

            ip.setId(UUID.randomUUID().toString());
            Meta meta = new Meta();
            meta.setProfile(Collections.singletonList(new CanonicalType("https://ig.hcxprotocol.io/v0.7/StructureDefinition-HCXInsurancePlan.html")));
            ip.setMeta(meta);
            ip.getIdentifier().add(new Identifier().setValue("AB_GUJ_Plan1"));
            ip.setStatus(Enumerations.PublicationStatus.ACTIVE);
            ip.setType(Collections.singletonList(new CodeableConcept().setCoding(Collections.singletonList(new Coding().setCode("medical").setSystem("http://terminology.hl7.org/CodeSystem/insurance-plan-type")))));
            ip.setName("PMJAY-Mukhyamantri Amrutam & Mukhyamantri Vatsalya");

            //Creating the Identification Extension Object
            HCXInsurancePlan.IdentificationExtension ext = new HCXInsurancePlan.IdentificationExtension();
            ext.setProofOfIdDocumentCode(new CodeableConcept(new Coding().setSystem("https://hcx-valuesets/proofOfIdentificationDocumentCodes").setCode("12345").setDisplay("Aadhar Card").setVersion("1.0.0")));
            ext.setDocumentationUrl(new UrlType("http://documentation-url"));
            ext.setClinicalDiagnosticDocumentClaimUse(new CodeType("preauthorization"));

            //Creating Presence Extension Object
            HCXInsurancePlan.PresenceExtension ext1 = new HCXInsurancePlan.PresenceExtension();
            ext1.setProofOfPresenceDocumentCode(new CodeableConcept(new Coding().setSystem("https://hcx-valuesets/proofOfPresenceDocumentCodes").setCode("12345").setVersion("1.0.0").setDisplay("Aadhar Verification XML")));
            ext1.setDocumentationUrl(new UrlType("http://documentation-url"));
            ext1.setClinicalDiagnosticDocumentClaimUse(new CodeType("preauthorization"));

            //Adding the above extensions to Insurance Plan Object
            HCXInsurancePlan.InsurancePlanPlanComponent plan = new HCXInsurancePlan.InsurancePlanPlanComponent();
            plan.setId(UUID.randomUUID().toString());
            plan.setIdentificationExtension(ext);
            plan.setPresenceExtension(ext1);
            plan.setType(new CodeableConcept().setCoding(Collections.singletonList(new Coding().setCode("PMJAY_GUJ_GOLD_CARD").setSystem("http://terminologyServer/ValueSets/plan-type"))));

            //specific cost
            InsurancePlan.InsurancePlanPlanSpecificCostComponent spc = new InsurancePlan.InsurancePlanPlanSpecificCostComponent();
            spc.setCategory(new CodeableConcept().setCoding(Collections.singletonList(new Coding().setCode("Inpatient-packages").setSystem("http://terminologyServer/ValueSets/cost-category"))));


            //specific cost benefit component
            HCXInsurancePlan.PlanBenefitComponent pbf = new HCXInsurancePlan.PlanBenefitComponent();
            pbf.setType(new CodeableConcept().setCoding(Collections.singletonList(new Coding().setCode("HBP_PACKAGE_00003").setSystem("http://terminologyServer/ValueSets/packages"))));

            //adding extensions to benefit component
            HCXInsurancePlan.DiagnosticDocumentsExtension dde =  new HCXInsurancePlan.DiagnosticDocumentsExtension();
            dde.setClinicalDiagnosticDocumentCode(new CodeableConcept(new Coding().setSystem("https://hcx-valuesets/proofOfIdentificationDocumentCodes").setCode("MAND0001").setVersion("1.0.0").setDisplay("Post Treatment clinical photograph")));
            dde.setDocumentationUrl(new UrlType("http://documentation-url"));
            dde.setClinicalDiagnosticDocumentClaimUse(new CodeType("claim"));

            HCXInsurancePlan.InformationalMessagesExtension ime =  new HCXInsurancePlan.InformationalMessagesExtension();
            ime.setInformationalMessagesCode(new CodeableConcept(new Coding().setSystem("https://hcx-valuesets/InformationalMessagesCodes").setCode("12343").setVersion("1.0.0").setDisplay("Information Message 1")));
            ime.setDocumentationUrl(new UrlType("http://documntation-url"));
            ime.setInformationalMessageClaimUse(new CodeType("claim"));

            HCXInsurancePlan.QuestionnairesExtension qe = new HCXInsurancePlan.QuestionnairesExtension();
            qe.setQuestionnaire(new Reference("Questionnnaire/1"));
            qe.setDocumentationUrl(new UrlType("http://documentation-url"));
            qe.setQuestionnaireClaimUse(new CodeType("claim"));

            pbf.setDiagnosticDocumentsExtension(dde);
            pbf.setInformationalMessagesExtension(ime);
            pbf.setQuestionnairesExtension(qe);


            InsurancePlan.PlanBenefitCostComponent pbc =  new InsurancePlan.PlanBenefitCostComponent();
            pbc.setType(new CodeableConcept().setCoding(Collections.singletonList(new Coding().setCode("hospitalization").setSystem("http://terminologyServer/ValueSets/pacakgeCostTypes"))));

            pbf.getCost().add(pbc);
            spc.getBenefit().add(pbf);

            plan.getSpecificCost().add(spc);
            ip.getPlan().add(plan);


            IParser p = FhirContext.forR4().newJsonParser().setPrettyPrint(true);
            String messageString = p.encodeResourceToString(ip);

            System.out.println("here is the json " + messageString);

            ValidationResult result1 = validator.validateWithResult(ip);
            for (SingleValidationMessage next : result1.getMessages()) {
                System.out.println(next.getSeverity() + " -- " + next.getLocationString() + " -- " + next.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
}
