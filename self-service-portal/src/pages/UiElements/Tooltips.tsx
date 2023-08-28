import React from 'react'
import Breadcrumb from '../../components/Breadcrumb'
import TooltipsOne from '../../components/TooltipsOne'
import TooltipsTwo from '../../components/TooltipsTwo'

const Tooltips: React.FC = () => {
  return (
    <>
      <Breadcrumb pageName="Tooltips" />

      <div className="flex flex-col gap-7.5">
        <TooltipsOne />
        <TooltipsTwo />
      </div>
    </>
  );
};

export default Tooltips
