
import { Link } from 'react-router-dom';

interface OnboardingSuccessProps {
    inviteUser: () => void
}

const OnboardingSuccess: React.FC<OnboardingSuccessProps> = ({inviteUser}:OnboardingSuccessProps) => {
    return (
        <>
            <div className="py-17.5 px-20 text-center">
                <Link className="mb-5.5 inline-block" to="#">
                    <svg
                        className="fill-primary"
                        width="60px"
                        height="60px"
                        viewBox="0 0 48 48"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <rect width={48} height={48} fill="white" fillOpacity={0.01} />
                        <path
                            d="M24 4L29.2533 7.83204L35.7557 7.81966L37.7533 14.0077L43.0211 17.8197L41 24L43.0211 30.1803L37.7533 33.9923L35.7557 40.1803L29.2533 40.168L24 44L18.7467 40.168L12.2443 40.1803L10.2467 33.9923L4.97887 30.1803L7 24L4.97887 17.8197L10.2467 14.0077L12.2443 7.81966L18.7467 7.83204L24 4Z"
                            fill=""
                            stroke="black"
                            strokeWidth={2}
                            strokeLinecap="round"
                            strokeLinejoin="round"
                        />
                        <path
                            d="M17 24L22 29L32 19"
                            stroke="white"
                            strokeWidth={4}
                            strokeLinecap="round"
                            strokeLinejoin="round"
                        />
                    </svg>
                </Link>
                <p className="2xl:px-15 py-5 font-bold text-l text-black dark:text-white">
                    Congratulations! Your Onboarding process has been successfully completed.
                </p>
                <p className="2xl:px-15 py-5 font-bold text-l text-black dark:text-white">
                    Activation link has been successfully sent to your registered email ID and Mobile No. Kindly verify for porfile activation.
                </p>
                <p className="2xl:px-15 py-5 font-bold text-l text-black dark:text-white">
                    You can start adding users to manage your Organization.
                </p>
            </div>
            <div className="mb-5">
        <input
          type="submit"
          value="Proceed to Inviting Users"
          className="w-full cursor-pointer rounded-lg border border-primary bg-primary p-4 text-white transition hover:bg-opacity-90"
          onClick={(event) => { event.preventDefault(); inviteUser(); }}
        />
      </div>
      <div className="flex mb-5 justify-end">
      <a href="/onboarding/profile" className="text-m text-primary underline">
                    Skip to Home
                  </a>
    </div>
        </>
    )
}

export default OnboardingSuccess;