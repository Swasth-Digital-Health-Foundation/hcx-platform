import React from 'react'
import Breadcrumb from '../../components/Breadcrumb'
import PaginationOne from '../../components/PaginationOne'
import PaginationTwo from '../../components/PaginationTwo'
import PaginationThree from '../../components/paginationThree'

const Pagination: React.FC = () => {
  return (
    <>
      <Breadcrumb pageName="Pagination" />

      <div className="flex flex-col gap-7.5">
        <PaginationOne />
        <PaginationTwo />
        <PaginationThree />
      </div>
    </>
  );
};

export default Pagination
