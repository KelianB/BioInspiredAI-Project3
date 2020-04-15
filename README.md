# Job Shop Scheduling Problem

This code attempts to minimize the makespan in a static [Job Shop Scheduling Problem](https://en.wikipedia.org/wiki/Job_shop_scheduling) (JSSP) using Particle Swarm Optimization (PSO) and Ant Colony Optimization (ACO).

Some relevant papers:
[Applying Ant Colony Optimization on dynamic JSSP](https://www.researchgate.net/publication/220650606_Applying_Ant_Colony_Optimisation_ACO_algorithm_to_dynamic_job_shop_scheduling_problems)
[Investigation of Particle Swarm Optimization for JSSP](https://ieeexplore.ieee.org/document/4344618)

This code was written as part of a project at NTNU (Norwegian University of Science and Technology) for the course IT3708 Bio-Inspired Artificial Intelligence.

## Results

This code gets makespans within 10% of the benchmarks given in the test-data.
Depending on the problem, some parameter tweaking can be required (see (config)[resources/config.properties]).