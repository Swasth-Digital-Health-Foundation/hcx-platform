import React from 'react'
import Breadcrumb from '../../components/Breadcrumb'
import NotificationsOne from '../../components/NotificationsOne'
import NotificationsTwo from '../../components/NotificationsTwo'
import NotificationsThree from '../../components/NotificationsThree'

const Notifications: React.FC = () => {
  return (
    <>
      <Breadcrumb pageName="Notifications" />

      <div className="flex flex-col gap-7.5">
        <NotificationsOne />
        <NotificationsTwo />
        <NotificationsThree />
      </div>
    </>
  );
};

export default Notifications
