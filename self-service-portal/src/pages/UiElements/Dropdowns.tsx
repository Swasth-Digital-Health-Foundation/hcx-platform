import React from 'react'
import Breadcrumb from '../../components/Breadcrumb'
import DropdownsTwo from '../../components/DropdownsTwo'
import DropdownsOne from '../../components/DropdownsOne'
import DropdownsThree from '../../components/DropdownsThree'

const Dropdowns: React.FC = () => {
  return (
    <>
      <Breadcrumb pageName="Dropdowns" />

      <div className="flex flex-col gap-7.5">
        <DropdownsOne />
        <DropdownsTwo />
        <DropdownsThree />
      </div>
    </>
  );
};

export default Dropdowns
