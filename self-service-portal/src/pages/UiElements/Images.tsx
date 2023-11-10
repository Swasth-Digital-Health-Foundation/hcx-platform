import React from 'react'
import Breadcrumb from '../../components/Breadcrumb'
import ImagesTwo from '../../components/ImagesTwo'
import ImagesOne from '../../components/ImagesOne'

const Images: React.FC = () => {
  return (
    <>
      <Breadcrumb pageName="Images" />

      <div className="flex flex-col gap-7.5">
        <ImagesOne />
        <ImagesTwo />
      </div>
    </>
  );
};

export default Images
