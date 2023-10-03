import React from 'react'
import Breadcrumb from '../../components/Breadcrumb'
import BadgeOne from '../../components/BadgeOne'
import BadgeTwo from '../../components/BadgeTwo'
import BadgeThree from '../../components/BadgeThree'
import BadgeFour from '../../components/BadgeFour'

const Badge: React.FC = () => {
  return (
    <>
      <Breadcrumb pageName="Badge" />

      <div className="flex flex-col gap-7.5">
        <BadgeOne />
        <BadgeTwo />
        <BadgeThree />
        <BadgeFour />
      </div>
    </>
  );
};

export default Badge
