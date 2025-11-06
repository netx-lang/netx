//
// Created by nairen on 29/08/2025.
//

#ifndef NXSIM_SIMULATION_H
#define NXSIM_SIMULATION_H


/**
 * Define NX_BACKEND before including circuit_def.h to specify the backend type for sized_mpint
 * Alternatives include:
 * - Primitive backends such as `uint32_t`, `uint64_t`
 * - Larger fixed-width backends such as `boost::multiprecision::uint128_t`, `boost::multiprecision::uint256_t`, etc.
 * - The arbitrary precision backend `boost::multiprecision::cpp_int`
*/
#ifndef NX_BACKEND
#define NX_BACKEND uint64_t
#endif

// The number of threads used in parallel simulation
#ifndef NX_NUM_THREADS
#define NX_NUM_THREADS 8
#endif

#include "circuit/circuit_def.h"
#include "circuit/circuit_parser.h"
#include "circuit/waveform_dump.h"
#include "circuit/parallel.h"

#endif //NXSIM_SIMULATION_H
