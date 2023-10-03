import React from 'react'
import Breadcrumb from '../../components/Breadcrumb'
import ButtonsGroupOne from '../../components/ButtonsGroupOne'
import ButtonsGroupTwo from '../../components/ButtonsGroupTwo'

const ButtonsGroup: React.FC = () => {
  return (
    <>
      <Breadcrumb pageName="Buttons Group" />

      <div className="flex flex-col gap-7.5">
        <ButtonsGroupOne />
        <ButtonsGroupTwo />
      </div>
    </>
  );
};

export default ButtonsGroup
