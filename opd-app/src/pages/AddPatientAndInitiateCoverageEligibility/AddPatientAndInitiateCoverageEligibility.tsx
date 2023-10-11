import React, { useEffect, useState } from "react";
import TextInputWithLabel from "../../components/inputField";
import SelectInput from "../../components/SelectInput";
import CustomButton from "../../components/CustomButton";
import { postRequest, updateRequest } from "../../services/registryService";
import { toast } from "react-toastify";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { generateOutgoingRequest } from "../../services/hcxMockService";
import { searchParticipant } from "../../services/hcxService";
import * as _ from "lodash";
import LoadingButton from "../../components/LoadingButton";

const AddPatientAndInitiateCoverageEligibility = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [name, setName] = useState<string>("");
  const [mobile, setMobile] = useState<string>("");
  const [address, setAddress] = useState<string>("");
  const [bloodGroup, setBloodGroup] = useState<string>("");
  const [allergies, setAllergies] = useState<string>("");
  const [payorName, setPayorName] = useState<string>("");
  const [insuranceID, setInsuranceID] = useState<string>("");
  const [loading, setLoading] = useState(false);
  const getEmailFromLocalStorage = localStorage.getItem("email");
  const [participantInfo, setParticipantInformation] = useState<any>([]);
  const [patientInfo, seetPatientInfo] = useState<any>([]);
  const [isEditable, setIsEditable] = useState<any>(false);

  const bloodGroupOptions = [
    {
      label: "Select",
      value: "",
    },
    { label: "O positive", value: "O positive" },
    { label: "O negative", value: "O negative" },
  ];
  const allergiesOptions = [
    {
      label: "Select",
      value: "",
    },
    { label: "cold", value: "cold" },
    { label: "caugh", value: "caugh" },
  ];
  const payorOptions = [
    {
      label: `${patientInfo[0]?.payor_details[0]?.payor || "Select"}`,
      value: patientInfo[0]?.payor_details[0]?.payor || "",
    },
    { label: "Swasth-reference payor", value: "Swast-reference payor" },
  ];

  const patientDataFromState: any = location.state?.obj;

  const payload = {
    name: patientDataFromState?.patientName || name || patientInfo[0]?.name,
    mobile: mobile || patientDataFromState?.mobile || patientInfo[0]?.mobile,
    address:
      address || patientDataFromState?.address || patientInfo[0]?.address,
    medical_history: [
      {
        allergies: allergies,
        bloodGroup: bloodGroup,
      },
    ],
    payor_details: [
      {
        insurance_id:
          insuranceID ||
          patientDataFromState?.payorName ||
          patientInfo[0]?.payor_details[0]?.payor,
        payor:
          payorName ||
          patientDataFromState?.insuranceId ||
          patientInfo[0]?.payor_details[0]?.insurance_id,
      },
    ],
  };

  const patientDetails = [
    {
      key: "Name :",
      value: patientDataFromState?.patientName,
    },
    {
      key: "Mobile no. :",
      value: patientDataFromState?.mobile,
    },
    {
      key: "Address :",
      value: patientDataFromState?.address,
    },
    {
      key: "Payor name :",
      value: patientDataFromState?.payorName,
    },
    {
      key: "Insurance ID :",
      value: patientDataFromState?.insuranceId,
    },
  ];

  const userSearchPayload = {
    entityType: ["Beneficiary"],
    filters: {
      primary_email: {
        eq: getEmailFromLocalStorage,
      },
    },
  };

  const token = localStorage.getItem("token");

  const config = {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };

  const search = async () => {
    try {
      const response = await searchParticipant(userSearchPayload, config);
      let userRes = response.data.participants;
      setParticipantInformation(userRes);
    } catch (error) {
      console.log(error);
    }
  };

  useEffect(() => {
    search();
    // patientSearch();
  }, []);

  // useEffect(() => {
  //   patientSearch();
  // }, [mobile]);
  // console.log(participantInfo);

  const patientSearchPayload = {
    entityType: ["Beneficiary"],
    filters: {
      mobile: { eq: mobile || patientDataFromState?.mobile },
    },
  };

  const registerUser = async () => {
    try {
      // setLoading(true);
      let registerResponse: any = await postRequest("invite", payload);
      setLoading(false);
      toast.success(
        "Patient added successfully, initiating coverage eligibility",
        {
          position: toast.POSITION.TOP_CENTER,
        }
      );
    } catch (error: any) {
      // setLoading(false);
      toast.info("Patient already exists,  initiating coverage eligibility", {
        position: toast.POSITION.TOP_CENTER,
      });
    }
  };

  const updateMedicalhistory = {
    medical_history: payload?.medical_history,
  };

  const updateMedicalHistory = async () => {
    try {
      let registerResponse: any = await updateRequest(
        `${patientInfo[0]?.osid}`,
        updateMedicalhistory
      );
    } catch (err) {
      console.log(err);
    }
  };

  const patientSearch = async () => {
    try {
      setLoading(true);
      let registerResponse: any = await postRequest(
        "search",
        patientSearchPayload
      );
      setIsEditable(true);
      const responseData = registerResponse.data;
      seetPatientInfo(responseData);
      setLoading(false);
      if (responseData.length === 0) {
        toast.error("Patient not found!");
        setIsEditable(false);
      } else {
        toast.success("Patient already exists!");
      }
    } catch (error: any) {
      setIsEditable(false);
      setLoading(false);
      toast.error("patient not found!", {
        position: toast.POSITION.TOP_CENTER,
      });
    }
  };

  localStorage.setItem("patientMobile", mobile);

  const coverageeligibilityPayload = {
    insuranceId:
      insuranceID ||
      patientDataFromState?.insuranceId ||
      patientInfo[0]?.payor_details[0]?.insurance_id,
    mobile: mobile || patientDataFromState?.mobile,
    payor:
      payorName ||
      patientDataFromState?.payorName ||
      patientInfo[0]?.payor_details[0]?.payor,
    providerName: localStorage.getItem("providerName"),
    participantCode: participantInfo[0]?.participant_code,
    serviceType: "OPD",
    patientName:
      name || patientDataFromState?.patientName || patientInfo[0]?.name,
  };

  const sendCoverageEligibilityRequest = async () => {
    try {
      setLoading(true);
      let response = await generateOutgoingRequest(
        "create/coverageeligibility/check",
        coverageeligibilityPayload
      );
      setLoading(false);
      if (response?.status === 202) {
        toast.success("Coverage eligibility initiated.");
        navigate("/add-consultation", {
          state: {
            patientMobile: patientDataFromState?.mobile,
            workflowId: response.data?.workflowId,
          },
        });
      }
    } catch (error) {
      setLoading(false);
      toast.error(_.get(error, "response.data.error.message"));
    }
  };

  // console.log(patientInfo[0].medical_history);
  return (
    <div>
      <label className="mb-2.5 block text-left text-2xl font-bold text-black dark:text-white">
        New patient details
      </label>
      {patientDataFromState ? (
        <div className='dark:bg-boxdark" rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark'>
          {patientDetails.map((ele: any) => {
            return (
              <div className="mb-2 flex gap-2">
                <h2 className="text-bold text-base font-bold text-black dark:text-white">
                  {ele.key}
                </h2>
                <span className="text-base font-medium">{ele.value}</span>
              </div>
            );
          })}
        </div>
      ) : (
        <div className="rounded-sm border border-stroke bg-white px-3 pb-3 shadow-default dark:border-strokedark dark:bg-boxdark">
          <label className="text-1xl mb-2.5 mt-2 block text-left font-bold text-black dark:text-white">
            Personal details : *
          </label>
          <div className="relative">
            <TextInputWithLabel
              label="Mobile no. :"
              value={mobile}
              onChange={(e: any) => setMobile(e.target.value)}
              placeholder="Enter mobile number"
              disabled={false}
              type="number"
            />
            <div className="absolute right-4 -mt-10">
              <a
                onClick={() => {
                  patientSearch();
                }}
                className="w-20 cursor-pointer py-2 font-medium text-black underline"
              >
                Search
              </a>
            </div>
          </div>
          <TextInputWithLabel
            label="Name :"
            value={name || patientInfo[0]?.name}
            onChange={(e: any) => setName(e.target.value)}
            placeholder="Enter patient name"
            disabled={false || isEditable}
            type="text"
          />
          <TextInputWithLabel
            label="Address :"
            value={address || patientInfo[0]?.address}
            onChange={(e: any) => setAddress(e.target.value)}
            placeholder="Enter address"
            disabled={false}
            type="text"
          />
        </div>
      )}
      <div className="mt-3">
        <div className="rounded-sm border border-stroke bg-white px-3 pb-3 shadow-default dark:border-strokedark dark:bg-boxdark">
          <label className="text-1xl mb-2.5 mt-2 block text-left font-bold text-black dark:text-white">
            Medical history : *
          </label>
          <SelectInput
            label="Blood group :"
            value={bloodGroup || patientInfo[0]?.medical_history?.blood_group}
            onChange={(e: any) => setBloodGroup(e.target.value)}
            disabled={false}
            options={bloodGroupOptions}
          />
          <SelectInput
            label="Allergies :"
            value={allergies || patientInfo[0]?.medical_history?.allergies}
            onChange={(e: any) => setAllergies(e.target.value)}
            disabled={false}
            options={allergiesOptions}
          />
        </div>
      </div>
      {patientDataFromState ? (
        <></>
      ) : (
        <div className="mt-3">
          <div className="rounded-sm border border-stroke bg-white px-3 pb-3 shadow-default dark:border-strokedark dark:bg-boxdark">
            <label className="text-1xl mb-2.5 mt-2 block text-left font-bold text-black dark:text-white">
              Insurance details : *
            </label>
            <SelectInput
              label="Payor Name :"
              value={payorName || patientInfo[0]?.payor_details[0]?.payor}
              onChange={(e: any) => setPayorName(e.target.value)}
              disabled={false || isEditable}
              options={payorOptions}
            />
            <TextInputWithLabel
              label="Insurance ID :"
              value={
                insuranceID || patientInfo[0]?.payor_details[0]?.insurance_id
              }
              onChange={(e: any) => setInsuranceID(e.target.value)}
              placeholder="Enter Insurance ID"
              disabled={false || isEditable}
              type="text"
            />
          </div>
        </div>
      )}
      {loading ? (
        <LoadingButton className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed" />
      ) : (
        <div>
          <CustomButton
            text="Add patient & Initiate consultation"
            onClick={() => {
              registerUser();
              updateMedicalHistory();
              sendCoverageEligibilityRequest();
            }}
            disabled={
              false
              // patientDataFromState
              //   ? allergies === '' || bloodGroup === ''
              //   : name === '' ||
              //     mobile === '' ||
              //     address === '' ||
              //     bloodGroup === '' ||
              //     allergies === '' ||
              //     payorName === '' ||
              //     insuranceID === ''
            }
          />
        </div>
      )}
    </div>
  );
};

export default AddPatientAndInitiateCoverageEligibility;
