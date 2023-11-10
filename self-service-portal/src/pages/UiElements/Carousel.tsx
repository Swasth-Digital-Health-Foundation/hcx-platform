import React from 'react'
import Breadcrumb from '../../components/Breadcrumb'
import CarouselTwo from '../../components/CarouselTwo'
import CarouselOne from '../../components/CarouselOne'
import CarouselThree from '../../components/CarouselThree'

const Carousel: React.FC = () => {
  return (
    <>
      <Breadcrumb pageName="Carousel" />

      <div className="flex flex-col gap-7.5">
        <CarouselOne />
        <CarouselTwo />
        <CarouselThree />
      </div>
    </>
  );
};

export default Carousel
