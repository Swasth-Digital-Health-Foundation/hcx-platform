import React from 'react'
import Breadcrumb from '../../components/Breadcrumb'
import AccordionOne from '../../components/AccordionOne'
import AccordionTwo from '../../components/AccordionTwo'

const Accordion: React.FC = () => {
  return (
    <>
      <Breadcrumb pageName="Accordion" />

      <div className="flex flex-col gap-7.5">
        <AccordionOne />
        <AccordionTwo />
      </div>
    </>
  );
};

export default Accordion
