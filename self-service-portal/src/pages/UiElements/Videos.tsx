import React from 'react'
import Breadcrumb from '../../components/Breadcrumb'
import VideosItem from '../../components/VideosItem'

const Videos: React.FC = () => {
  return (
    <>
      <Breadcrumb pageName="Videos" />

      <div className="flex flex-col gap-7.5">
        <VideosItem title="Embeds Video" embeds />
        <VideosItem title="Responsive Aspect ratios 4:3" aspectFour />
        <VideosItem title="Responsive Aspect ratios 21:9" aspectTwentyOne />
        <VideosItem title="Responsive Aspect ratios 1:1" aspectOne />
      </div>
    </>
  );
};

export default Videos
