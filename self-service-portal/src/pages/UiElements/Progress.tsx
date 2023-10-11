import React from 'react'
import Breadcrumb from '../../components/Breadcrumb'
import ProgressOne from '../../components/ProgressOne'
import ProgressTwo from '../../components/ProgressTwo'
import ProgressThree from '../../components/ProgressThree'
import ProgressFour from '../../components/ProgressFour'

const Progress: React.FC = () => {
  return (
    <>
      <Breadcrumb pageName="Progress" />

      <div className="flex flex-col gap-7.5">
        <ProgressOne />
        <ProgressTwo />
        <ProgressThree />
        <ProgressFour />
      </div>
    </>
  );
};

export default Progress
