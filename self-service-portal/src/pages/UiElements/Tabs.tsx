import React from 'react';
import Breadcrumb from '../../components/Breadcrumb';
import TabOne from '../../components/TabOne';
import TabTwo from '../../components/TabTwo';
import TabThree from '../../components/TabThree';

const Tabs: React.FC = () => {
  return (
    <>
      <Breadcrumb pageName="Tabs" />

      <div className="flex flex-col gap-9">
        <TabOne />
        <TabTwo />
        <TabThree />
      </div>
    </>
  );
};

export default Tabs;
