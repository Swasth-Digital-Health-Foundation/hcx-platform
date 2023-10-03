import React from 'react'
import Breadcrumb from '../../components/Breadcrumb'
import PopoversOne from '../../components/PopoversOne'
import PopoversTwo from '../../components/PopoversTwo'

const Popovers: React.FC = () => {
  return (
    <>
      <Breadcrumb pageName="Popovers" />

      <div className="flex flex-col gap-7.5">
        <PopoversOne />
        <PopoversTwo />
      </div>
    </>
  );
};

export default Popovers
